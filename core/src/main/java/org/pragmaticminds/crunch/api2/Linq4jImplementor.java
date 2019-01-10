/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.pragmaticminds.crunch.api2;

import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.pragmaticminds.crunch.api.pipe.ClonerUtil;
import org.pragmaticminds.crunch.api.pipe.EvaluationFunction;
import org.pragmaticminds.crunch.api.pipe.SimpleEvaluationContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class Linq4jImplementor {

  private final Enumerable<?> input;
  private final Predicate<?> predicate;
  private final Function<?, Object> groupAssigner;
  private final List<EvaluationANdHandler<?>> evaluations;

  public Linq4jImplementor(Root<T> root) {
    final ImplementingVisitor implementingVisitor = new ImplementingVisitor();
    root.accept(implementingVisitor);
    this.input = implementingVisitor.input;
    this.predicate = implementingVisitor.predicate;
    this.groupAssigner = implementingVisitor.groupAssigner;
    this.evaluations = implementingVisitor.eh;
  }

  public Enumerator<Void> implement() {
    Enumerable<?> datastream;
    if (predicate != null) {
      datastream = input.where(predicate::test);
    } else {
      datastream = input;
    }
    // Start the evaluation
    return implementEvaluation(datastream, groupAssigner, evaluations);
  }

  private Enumerator<Void> implementEvaluation(Enumerable<?> in, Function<?, Object> groupAssigner, List<EvaluationANdHandler<?>> evaluations) {
    // Need state for each group and each Evaluation...
    Map<GroupEvaluation<?>, EvaluationFunction<?>> states = new HashMap<>();
    final Enumerator<?> enumerator = in.enumerator();
    return new Enumerator<Void>() {

      @Override public Void current() {
        return null;
      }

      @Override public boolean moveNext() {
        // Evaluate until an event is found
        while (enumerator.moveNext()) {
          // Get hash
          final Object hash = groupAssigner.apply(enumerator.current());
          // For each evaluation
          for (EvaluationANdHandler<?> eh : evaluations) {
            final SimpleEvaluationContext<?> ctx = new SimpleEvaluationContext<>(enumerator.current());
            final GroupEvaluation<?> group = new GroupEvaluation<>(hash, eh.evaluation);
            states.computeIfAbsent(group, g -> ClonerUtil.clone(g.evaluation));
            states.get(group).eval(ctx);

            for (Object event : ctx.getEvents()) {
              eh.handler.accept(event);
            }
          }
          return true;
        }
        return false;
      }

      @Override public void reset() {

      }

      @Override public void close() {

      }
    };
  }

  public static class EvaluationANdHandler<EVENT extends Serializable> {

    private final EvaluationFunction<EVENT> evaluation;
    private final Consumer<EVENT> handler;

    public EvaluationANdHandler(EvaluationFunction<EVENT> evaluation, Consumer<EVENT> handler) {
      this.evaluation = evaluation;
      this.handler = handler;
    }

    public EvaluationFunction<EVENT> getEvaluation() {
      return evaluation;
    }

    public Consumer<EVENT> getHandler() {
      return handler;
    }
  }

  public static class GroupEvaluation<T extends Serializable> {

    private final Object hash;
    private final EvaluationFunction<T> evaluation;

    public GroupEvaluation(Object hash, EvaluationFunction<T> evaluation) {
      this.hash = hash;
      this.evaluation = evaluation;
    }

    public Object getHash() {
      return hash;
    }

    public EvaluationFunction<T> getEvaluation() {
      return evaluation;
    }

    @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      GroupEvaluation<?> that = (GroupEvaluation<?>) o;
      return Objects.equals(hash, that.hash) &&
          Objects.equals(evaluation, that.evaluation);
    }

    @Override public int hashCode() {
      return Objects.hash(hash, evaluation);
    }
  }

  private static class ImplementingVisitor implements StreamNodeVisitor<Void> {

    private Enumerable<?> input;
    private Predicate<?> predicate;
    private Function<?, Object> groupAssigner;
    private List<EvaluationANdHandler<?>> eh = new ArrayList<>();

    @Override public <IN> Void visit(Root<IN> root) {
      input = root.getValues();
      root.getChildren().forEach(c -> c.accept(this));
      return null;
    }

    @Override public <IN> Void visit(Filter<IN> filter) {
      predicate = filter.predicate;
      filter.getChildren().forEach(c -> c.accept(this));
      return null;
    }

    @Override public <KEY, IN> Void visit(GroupBy<KEY, IN> groupBy) {
      groupAssigner = groupBy.groupAssigner;
      groupBy.getChildren().forEach(c -> c.accept(this));
      return null;
    }

    @Override public <IN, EVENT extends Serializable> Void visit(Evaluate<IN, EVENT> evaluate) {
      final EvaluationFunction<EVENT> evaluation = evaluate.getEvaluation();
      for (StreamNode<EVENT, ?> child : evaluate.getChildren()) {
        assert child instanceof ResultHandler;
        this.eh.add(new EvaluationANdHandler<>(evaluation, ((ResultHandler<EVENT>) child).getConsumer()));
      }
      return null;
    }

    @Override public <EVENT> Void visit(ResultHandler<EVENT> handler) {
      throw new IllegalStateException("This should not happen!");
    }

  }
}