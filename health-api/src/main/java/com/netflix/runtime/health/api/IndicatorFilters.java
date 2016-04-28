/**
 * Copyright 2016 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package com.netflix.runtime.health.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Static builder of for creating an {@link IndicatorFilter} instance.
 * 
 * <pre>
 *  IndicatorFilters.includes(someIndicatorInstance)
 *                  .excludes("someIndicatorName")
 *                  .build();
 * </pre>
 */
public class IndicatorFilters {
    
    public static IndicatorFilterBuilder includes(String... indicatorNames) {
        return new IndicatorFilterBuilder().includes(indicatorNames);
    }
    
    public static IndicatorFilterBuilder excludes(String... indicatorNames) {
        return new IndicatorFilterBuilder().excludes(indicatorNames);
    }
    
    public static IndicatorFilter build() {
        return i -> true;
    }

    public static class IndicatorFilterBuilder {
        
        public final List<String> includedIndicatorNames;
        public final List<String> excludedIndicatorNames;
        
        public IndicatorFilterBuilder() {
            this.includedIndicatorNames = new ArrayList<>();
            this.excludedIndicatorNames = new ArrayList<>();
        }
        
        public IndicatorFilterBuilder includes(String... indicatorNames) {
            if (indicatorNames != null) {
                includedIndicatorNames.addAll(Arrays.asList(indicatorNames));
            }
            return this;
        }
        
        public IndicatorFilterBuilder excludes(String... indicatorNames) {
            if (indicatorNames != null) {
                excludedIndicatorNames.addAll(Arrays.asList(indicatorNames));
            }
            return this;
        }
        
        public IndicatorFilter build() {
            return indicatorName -> {
               Optional<Boolean> include = includedIndicatorNames.stream().map(included -> indicatorName.equals(included)).reduce((a,b) -> a || b);
               Optional<Boolean> exclude = excludedIndicatorNames.stream().map(excluded -> indicatorName.equals(excluded)).reduce((a,b) -> a || b);
               if(exclude.isPresent() && exclude.get()) {
                   return false;
               }
               if(include.isPresent() && !include.get()) {
                   return false;
               }
               return true;
            };
        }
        
    }
}

