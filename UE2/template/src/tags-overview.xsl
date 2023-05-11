<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
    <xsl:output method="html"/>
    <!-- Insert the XSLT Stylesheet here -->
    <xsl:template match="shipment">
        <html>
            <head>
                <title>Shipment - Tags Overview</title>
                <style>
                    body {
                    font-family: Verdana, sans-serif;
                    }
                    table {
                    font-family: Verdana, sans-serif;
                    border-collapse: collapse;
                    width: 31em;
                    }

                    td, th {
                    border: 1px solid #080808;
                    padding: 8px;
                    vertical-align: top;
                    }

                    tr:hover {background-color: #fdf3fd;}

                    th {
                    padding-top: 2px;
                    padding-bottom: 2px;
                    text-align: center;
                    background-color: #6c3b6c;
                    color: white;
                    }
                </style>
            </head>
            <body>
                <h1>Tags Overview</h1>
                <hr/>
                <xsl:apply-templates select="//t[@tagname]"/>
            </body>
        </html>
    </xsl:template>

    <!-- Insert additional templates here -->
    <xsl:template match="t">
        <h2>
            <xsl:value-of select="@tagname"/>
        </h2>
        <p>
            <b>Description: </b><br/>
            <xsl:choose>
                <xsl:when test="normalize-space(.)">
                    <xsl:value-of select="."/>
                </xsl:when>
                <xsl:otherwise>
                    No further information on <xsl:value-of select="@tagname"/>.
                </xsl:otherwise>
            </xsl:choose>
        </p>
            Below is a list of all Ships or Products tagged with <xsl:value-of select="@tagname"/>, either directly, or indirectly via association from their transportation.
        <table>
            <tr>
                <xsl:call-template name="info">
                    <xsl:with-param name="mode" select="'ship'"/>
                    <xsl:with-param name="tagname" select="@tagname"/>
                </xsl:call-template>
                <xsl:call-template name="info">
                    <xsl:with-param name="mode" select="'products'"/>
                    <xsl:with-param name="tagname" select="@tagname"/>
                </xsl:call-template>
            </tr>
        </table>
    </xsl:template>

    <xsl:template name="info">
        <xsl:param name="mode"/>
        <xsl:param name="tagname"/>

        <xsl:choose>
            <xsl:when test="$mode = 'ship'">
                <xsl:variable name="matching_ships"
                              select="//ship[tags/tag = $tagname or product[tag = $tagname and label/ref/@sid = current()/@sid]]"/>
                <xsl:if test="$matching_ships">
                    <td>
                        <table>
                            <tr>
                                <th>Ships</th>
                            </tr>
                            <xsl:for-each select="$matching_ships">
                                <xsl:sort select="name"/>
                                        <tr>
                                            <td>
                                                <xsl:apply-templates select="."/>
                                            </td>
                                        </tr>
                            </xsl:for-each>
                            <tr>
                                <td colspan="2"><b>Found: </b>
                                    <xsl:value-of select="count($matching_ships)"/> Ship(s)
                                </td>
                            </tr>
                        </table>
                    </td>
                </xsl:if>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="matching_products"
                              select="//product[tags/tag = $tagname or ship[tag = $tagname and @sid = current()/@ref]]"/>
                <xsl:if test="$matching_products">
                    <td>
                        <table>
                            <tr>
                                <th>Products</th>
                            </tr>
                            <xsl:for-each select="$matching_products">
                                <xsl:sort select="label/destination[1]"/>
                                <tr>
                                    <td>
                                        <xsl:apply-templates select="."/>
                                    </td>
                                </tr>
                            </xsl:for-each>
                            <tr>
                                <td colspan="2"><b>Found: </b>
                                    <xsl:value-of select="count($matching_products)"/> Product(s)
                                </td>
                            </tr>
                        </table>
                    </td>
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="ship">
        <p>
            <xsl:value-of select="name"/>
            (
            <xsl:value-of select="info/@firstTour"/>
            <xsl:if test="info/@lastTour">
                <xsl:text> to </xsl:text>
                <xsl:value-of select="info/@lastTour"/>
            </xsl:if>, constructed in
            <xsl:value-of select="info/@placeOfConstruction"/> )</p>
    </xsl:template>


    <xsl:template match="product">
        <b>Title: </b><xsl:value-of select="name"/>, <b>Type: </b><xsl:value-of select="upper-case(type/*/name())"/>
        <p>
            <b>Label:</b>
            <br/>
            <xsl:value-of select="label"/>
        </p>
        <xsl:choose>
            <xsl:when test="label/ref/@sid = //ship/@sid">
                <b>Transported By:</b>
                <ul>
                    <xsl:for-each select="//ship[@sid=current()/label/ref/@sid]">
                        <li>
                            <xsl:apply-templates select="."/>
                        </li>
                    </xsl:for-each>
                </ul>
            </xsl:when>
            <xsl:otherwise>
                <p>No shipment detail.</p>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
