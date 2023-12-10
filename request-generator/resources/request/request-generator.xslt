<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:servicestype="http://xmlns.ec.eu/BusinessObjects/IOSS_DR/Common/V1">
    <xsl:output method="xml" indent="yes"/>

    <xsl:param name="count" select="0"/>
    <xsl:param name="sequence" select="0"/>


    <xsl:template match="servicestype:iossVatNumberUpdate" name="repeat">
        <xsl:param name="createCount" select="$count" />
        <xsl:if test="$createCount">
            <xsl:call-template name="root">
                <xsl:with-param name="sequenceNumber" select="$createCount + $sequence"></xsl:with-param>
            </xsl:call-template>
            <xsl:call-template name="repeat">
                <xsl:with-param name="createCount" select="$createCount - 1" />
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <!-- the identity template (copies your input verbatim) -->
    <xsl:template match="node() | @*" name="root">
        <xsl:param name="sequenceNumber" />
        <xsl:copy>
            <xsl:apply-templates select="node() | @*">
                <xsl:with-param name="sequenceNumber" select="$sequenceNumber"></xsl:with-param>
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>

    <!-- special templates only for things that need them -->

    <xsl:template match="servicestype:iossVatId">
        <xsl:param name="sequenceNumber" />
        <servicestype:iossVatId>IM<xsl:value-of select="format-number($sequenceNumber, '0000000000')" /></servicestype:iossVatId>
    </xsl:template>


</xsl:stylesheet>