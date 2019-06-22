/**
 * Copyright 2018-present febit.org (support@febit.org)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.rectify;

public class TestSchemas {

    public static final Schema COMPLEX = Schema.parseFieldLines("demo", "\n"
            + "# comment\n"
            + "  \n"
            + " int id  \n"
            + " sTring name\n"
            + " array<int> ints  # \n"
            + " float float  # \n"
            + " double double  # \n"
            + " ArRAy < string >   strings  # comment test \n "
            + " map<long> longMap\n"
            + "    #  comment\n"
            + " map < string> stringMap \n"
            + " optional<map < string>> optionalStringMap \n"
            + " struct<id:string,launch:bigint,du:long,date:int> session\n"
            + " array<struct< du: bigint, name : string  ,  ts:optional<bigint>, attrs:map<String> , struct:struct<xx:String>, flag:boolean>  >  events\n"
            + "");
}
