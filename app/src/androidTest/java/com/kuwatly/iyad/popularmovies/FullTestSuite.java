/*
 * Copyright (C) 2016 Iyad Kuwatly
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kuwatly.iyad.popularmovies;



import android.app.Activity;
import android.content.Context;
import android.test.suitebuilder.TestSuiteBuilder;

import com.facebook.stetho.InspectorModulesProvider;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.inspector.protocol.ChromeDevtoolsDomain;
import com.facebook.stetho.rhino.JsRuntimeReplFactoryBuilder;

import junit.framework.Test;
import junit.framework.TestSuite;

public class FullTestSuite extends TestSuite {
    final static Context mContext = new Activity();
    public static Test suite() {

        Stetho.initialize(
                Stetho.newInitializerBuilder(mContext)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(mContext))
                        .enableWebKitInspector(new InspectorModulesProvider() {
                            @Override
                            public Iterable<ChromeDevtoolsDomain> get() {
                                return new Stetho.DefaultInspectorModulesBuilder(mContext).runtimeRepl(
                                        new JsRuntimeReplFactoryBuilder(mContext)
                                                // Pass to JavaScript: var foo = "bar";
                                                .addVariable("foo", "bar")
                                                .build()
                                ).finish();
                            }
                        })
                        .build());


        return new TestSuiteBuilder(FullTestSuite.class)
                .includeAllPackagesUnderHere().build();
    }

    public FullTestSuite() {
        super();
    }
}