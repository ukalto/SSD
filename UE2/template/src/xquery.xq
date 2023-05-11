(: Your XQuery goes here :)

<largeOrdersOnShips>
    {
        let $ships := /shipment/ships/ship
        let $products := /shipment/products/product
        for $ship in $ships
        let $labels := ($products/label[ref/@sid = $ship/@sid])
        let $destinations := distinct-values($labels/destination)
        order by xs:date($ship/info/@firstTour)
        for $destination in $destinations
        where count($products[label/destination = $destinations]) > 3
        return
            <ship name="{$ship/name}" destination="{$destination}">
                <productCount>{count($products[label/destination = $destinations])}</productCount>
                <products>
                    {
                        for $product in $products[label/destination = $destinations]
                        return <product>{$product/name/text()}</product>
                    }
                </products>
            </ship>
    }
</largeOrdersOnShips>
