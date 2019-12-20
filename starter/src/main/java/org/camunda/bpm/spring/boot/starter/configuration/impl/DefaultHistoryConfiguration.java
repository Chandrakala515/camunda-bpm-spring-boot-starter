/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.spring.boot.starter.configuration.impl;

import org.camunda.bpm.engine.impl.history.handler.CompositeDbHistoryEventHandler;
import org.camunda.bpm.engine.impl.history.handler.CompositeHistoryEventHandler;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.configuration.CamundaHistoryConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultHistoryConfiguration extends AbstractCamundaConfiguration implements CamundaHistoryConfiguration {

  @Autowired(required = false)
  protected HistoryEventHandler historyEventHandler;

  @Override
  public void preInit(SpringProcessEngineConfiguration configuration) {
    String historyLevel = camundaBpmProperties.getHistoryLevel();
    if (historyLevel != null) {
      configuration.setHistory(historyLevel);
    }

    HistoryEventHandler existingHandler =  configuration.getHistoryEventHandler();
    if (historyEventHandler != null) {
      logger.debug("registered history event handler: {}", historyEventHandler.getClass());
      // it is assumed that, when a historyEventHandler bean is provided,
      // the intention is to override the default DbHistoryEventHandler
      CompositeHistoryEventHandler compositeHandler = new CompositeHistoryEventHandler(historyEventHandler);
      if (existingHandler != null) {
        compositeHandler.add(existingHandler);
      }
      configuration.setHistoryEventHandler(compositeHandler);
    } else {
      logger.debug("registered history event handler: {}", CompositeDbHistoryEventHandler.class);
      // if no historyEventHandler bean is provided, the default DbHistoryEventHandler will
      // be included together with any HistoryEventHandlers added through other Spring Beans.
      CompositeHistoryEventHandler compositeDbHandler = new CompositeDbHistoryEventHandler();
      if (existingHandler != null) {
        compositeDbHandler.add(existingHandler);
      }
      configuration.setHistoryEventHandler(compositeDbHandler);
    }
  }

}
