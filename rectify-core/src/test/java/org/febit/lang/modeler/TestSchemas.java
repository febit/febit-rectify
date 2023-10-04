/*
 * Copyright 2018-present febit.org (support@febit.org)
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
package org.febit.lang.modeler;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestSchemas {

    public static final Schema COMPLEX = Schema.parseStruct("demo", "\n"
            , "# comment\n"
            , "  \n"
            , " int id  \n"
            , " sTring name\n"
            , " array<int> ints  # \n"
            , " float float  # \n"
            , " double double  # \n"
            , " ArRAy < string >   strings  # comment test \n "
            , " map<string,long> longMap\n"
            , "    #  comment\n"
            , " map <string, string> stringMap \n"
            , " optional<map <string, string>> optionalStringMap \n"
            , " struct<id:string,launch:int64,du:long,date:int> session\n"
            , " array<struct< du: int64, name : string  ,  ts:optional<int64>, attrs:map<string,String> , struct:struct<xx:String>, flag:boolean>  >  events\n"
            , " array<struct<time:time,date:date,dt:datetime,dtz:datetimetz,instant:instant>> times"
            , "");
}
