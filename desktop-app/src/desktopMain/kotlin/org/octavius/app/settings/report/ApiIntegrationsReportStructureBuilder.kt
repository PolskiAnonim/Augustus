package org.octavius.app.settings.report

import io.github.octaviusframework.client.query.QueryFragment
import org.octavius.app.localization.AppTr
import org.octavius.report.column.ReportColumn
import org.octavius.report.column.type.BooleanColumn
import org.octavius.report.column.type.IntegerColumn
import org.octavius.report.column.type.StringColumn
import org.octavius.report.component.ReportStructureBuilder

class ApiIntegrationsReportStructureBuilder() : ReportStructureBuilder() {

    override fun getReportName(): String = "apiIntegrations"

    override fun buildQuery(): QueryFragment = QueryFragment(
        """
            SELECT id, name, enabled, api_key, endpoint_url, port, last_sync
            FROM api_integrations
            ORDER BY name
            """.trimIndent()
    )

    override fun buildColumns(): Map<String, ReportColumn> = mapOf(
        "name" to StringColumn(AppTr.Settings.Api.Columns.name(), filterable = true),
        "enabled" to BooleanColumn(AppTr.Settings.Api.Columns.enabled(), filterable = true),
        "api_key" to StringColumn(AppTr.Settings.Api.Columns.apiKey(), filterable = false),
        "endpoint_url" to StringColumn(
            AppTr.Settings.Api.Columns.endpointUrl(),
            filterable = false
        ),
        "port" to IntegerColumn(AppTr.Settings.Api.Columns.port(), filterable = true),
        "last_sync" to StringColumn(
            AppTr.Settings.Api.Columns.lastSync(),
            filterable = false
        )
    )
}