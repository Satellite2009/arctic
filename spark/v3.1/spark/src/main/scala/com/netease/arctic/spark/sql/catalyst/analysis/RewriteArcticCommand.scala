/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.spark.sql.catalyst.analysis

import com.netease.arctic.spark.sql.ArcticExtensionUtils.isArcticTable
import com.netease.arctic.spark.sql.catalyst.plans.{AlterArcticTableDropPartition, TruncateArcticTable}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.analysis.ResolvedTable
import org.apache.spark.sql.catalyst.plans.logical.{AlterTableDropPartition, LogicalPlan, TruncateTable}
import org.apache.spark.sql.catalyst.rules.Rule

/**
 * Rule for rewrite some spark commands to arctic's implementation.
 * @param sparkSession
 */
case class RewriteArcticCommand(sparkSession: SparkSession) extends Rule[LogicalPlan] {
  override def apply(plan: LogicalPlan): LogicalPlan = {
    plan match {
      // Rewrite the AlterTableDropPartition to AlterArcticTableDropPartition
      case a@AlterTableDropPartition(r: ResolvedTable, parts, ifExists, purge, retainData)
        if isArcticTable(r.table) =>
        AlterArcticTableDropPartition(a.child, parts, ifExists, purge, retainData)
      case t@TruncateTable(r: ResolvedTable, partitionSpec)
        if isArcticTable(r.table) =>
        TruncateArcticTable(t.child, partitionSpec)
      case _ => plan
    }
  }
}