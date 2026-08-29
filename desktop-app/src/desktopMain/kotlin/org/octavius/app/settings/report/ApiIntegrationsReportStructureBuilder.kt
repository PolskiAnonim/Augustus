package org.octavius.app.settings.report

import io.github.octaviusframework.client.query.QueryFragment
import org.octavius.localization.Tr
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
        "name" to StringColumn(Tr.Settings.Api.Columns.name(), filterable = true),
        "enabled" to BooleanColumn(Tr.Settings.Api.Columns.enabled(), filterable = true),
        "api_key" to StringColumn(Tr.Settings.Api.Columns.apiKey(), filterable = false),
        "endpoint_url" to StringColumn(
            Tr.Settings.Api.Columns.endpointUrl(),
            filterable = false
        ),
        "port" to IntegerColumn(Tr.Settings.Api.Columns.port(), filterable = true),
        "last_sync" to StringColumn(
            Tr.Settings.Api.Columns.lastSync(),
            filterable = false
        )
    )
}