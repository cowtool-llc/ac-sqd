/*global SqdCalculator _config*/

var SqdCalculator = window.SqdCalculator || {};

(function flightStatusScopeWrapper($) {
    var authToken;
    var authTokenLoaded = false;
    var isDocReady = false;
    SqdCalculator.authToken.then(function setAuthToken(token) {
        if (token) {
            authToken = token;
        }
        authTokenLoaded = true;
        performOnLoad();
    }).catch(function handleTokenError(error) {
        alert(error);
    });

    function callLambda(
        ticket,
        aeroplanStatus,
        hasBonusPointsPrivilege,
        segments,
        baseFare,
        surcharges
    ) {
        $('#calculateSqd').buttonLoader('start');

        AWS.config.region = _config.cognito.region;
        AWS.config.credentials = new AWS.CognitoIdentityCredentials({
            IdentityPoolId: _config.cognito.identityPoolId,
            Logins: {
                ['cognito-idp.' + _config.cognito.region + '.amazonaws.com/' + _config.cognito.userPoolId]: authToken
            }
        });

        lambda = new AWS.Lambda({region: 'us-east-1'});
        const pullParams = {
            FunctionName : _config.lambda.functionName,
            InvocationType : 'RequestResponse',
            LogType : 'None',
            Payload : JSON.stringify({
                ticket : ticket,
                aeroplanStatus : aeroplanStatus,
                hasBonusPointsPrivilege : hasBonusPointsPrivilege,
                segments : segments,
                baseFare : baseFare,
                surcharges : surcharges,
                authToken : authToken
            })
        };

        lambda.invoke(pullParams, function(err, data) {
            if (err) {
                alert(err);
            } else {
                const response = JSON.parse(data.Payload);
                if (response.errorMessage) {
                    alert(response.errorMessage);
                } else {
                    const results = response.results;
                    populateResults(response.itinerary);
                    populateUrl(ticket, aeroplanStatus, hasBonusPointsPrivilege, segments, baseFare, surcharges);
                }
            }

            $('#calculateSqd').buttonLoader('stop');
        });
    }

    function populateResults(itinerary) {
        $('#resultsContainer').show();
        $("#resultsTable > tbody").empty();
        $("#resultsTable > tfoot").empty();

        itinerary.segments.forEach(segment => {
            $("#resultsTable").find('tbody')
                .append($('<tr>')
                    .append($('<td>')
                        .text(segment.airline)
                    )
                    .append($('<td>')
                        .text(segment.origin + '-' + segment.destination)
                    )
                    .append($('<td>')
                        .text(segment.fareClass + ('fareBrand' in segment ? ' (' + segment.fareBrand + ')' : ''))
                        .attr('align', 'center')
                    )
                    .append($('<td>')
                        .text(segment.earningResult.distanceResult.distance.toLocaleString('en-US'))
                        .attr('align', 'right')
                    )
                    .append($('<td>')
                        .text(segment.earningResult.distanceResult.source)
                        .attr('align', 'center')
                    )
                    .append($('<td>')
                        .text(segment.earningResult.sqmPercent)
                        .attr('align', 'right')
                        .css('background-color', segment.earningResult.sqmPercentEstimated ? '#FFFF00' : 'transparent')
                    )
                    .append($('<td>')
                        .text(segment.earningResult.sqm.toLocaleString('en-US'))
                        .attr('align', 'right')
                    )
                    .append($('<td>')
                        .text(segment.earningResult.sqd.toLocaleString('en-US'))
                        .attr('align', 'right')
                    )
                    .append($('<td>')
                        .text(segment.earningResult.aeroplanPointsPercent)
                        .attr('align', 'right')
                    )
                    .append($('<td>')
                        .text(segment.earningResult.aeroplanMiles.toLocaleString('en-US'))
                        .attr('align', 'right')
                    )
                    .append($('<td>')
                        .text(segment.earningResult.bonusPointsPercent)
                        .attr('align', 'right')
                    )
                    .append($('<td>')
                        .text(segment.earningResult.bonusPoints)
                        .attr('align', 'right')
                    )
                    .append($('<td>')
                        .text(segment.earningResult.totalMiles)
                        .attr('align', 'right')
                    )
                )
        });

        const totalPoints = (itinerary.totalRow.totalPoints != null) ? itinerary.totalRow.totalPoints.toLocaleString('en-US') : '???';
        $("#resultsTable").find('tfoot')
            .append($('<tr>')
                .css('font-weight', 'bold')
                .append($('<td>')
                    .text("Total")
                    .attr('align', 'center')
                    .attr('colspan', 2)
                )
                .append($('<td>')
                )
                .append($('<td>')
                    .text(itinerary.totalRow.distance.toLocaleString('en-US'))
                    .attr('align', 'right')
                )
                .append($('<td>')
                    .attr('colspan', 2)
                )
                .append($('<td>')
                    .text(itinerary.totalRow.sqm.toLocaleString('en-US'))
                    .attr('align', 'right')
                )
                .append($('<td>')
                    .text(itinerary.totalRow.sqd.toLocaleString('en-US'))
                    .attr('align', 'right')
                )
                .append($('<td>')
                )
                .append($('<td>')
                    .text(itinerary.totalRow.aeroplanMiles.toLocaleString('en-US'))
                    .attr('align', 'right')
                )
                .append($('<td>')
                )
                .append($('<td>')
                    .text(itinerary.totalRow.bonusPoints.toLocaleString('en-US'))
                    .attr('align', 'right')
                )
                .append($('<td>')
                    .text(itinerary.totalRow.totalMiles.toLocaleString('en-US'))
                    .attr('align', 'right')
                )
            )
    }

    function populateUrl(
        ticket,
        aeroplanStatus,
        hasBonusPointsPrivilege,
        segments,
        baseFare,
        surcharges
    ) {
        let queryParams = "?ticket=" + ticket +
            "&aeroplanStatus=" + aeroplanStatus +
            "&hasBonusPointsPrivilege=" + hasBonusPointsPrivilege +
            "&segments=" + encodeURIComponent(segments) +
            "&baseFare=" + baseFare +
            "&surcharges=" + surcharges;
        window.history.pushState({"queryParams":queryParams}, "", queryParams);
    }

    $(function onDocReady() {
        $('#calculateSqd').click(handleRequestClick);

        isDocReady = true;
        performOnLoad();
    });

    function performOnLoad() {
        if (!isDocReady || !authTokenLoaded) {
            return;
        }

        if (authToken != null) {
            const tokens = authToken.split(".");
            const payload = JSON.parse(atob(tokens[1]));
            const email = payload.email;
            $('#username').text(email);
            $('#signedInContainer').show();
            $('#notSignedInContainer').hide();
        } else {
            $('#signedInContainer').hide();
            $('#notSignedInContainer').show();
        }

        const shouldCalculate = populateFields();

        if (shouldCalculate) {
            performCalculateSqd();
        }
    }

    function performCalculateSqd() {
        const ticket = $('#ticket').val();
        const aeroplanStatus = $('#aeroplanStatus').val();
        const hasBonusPointsPrivilege = $('#hasBonusPointsPrivilege').is(':checked');
        const segments = $('#segments').val();
        const baseFare = $('#baseFare').val();
        const surcharges = $('#surcharges').val();
        callLambda(ticket, aeroplanStatus, hasBonusPointsPrivilege, segments, baseFare, surcharges);
    }

    function handleRequestClick(event) {
        event.preventDefault();
        const form = $('#sqdForm')[0];
        form.reportValidity();
        if (form.checkValidity()) {
            performCalculateSqd();
        }
    }

    function populateFields() {
        const urlParams = new URLSearchParams(window.location.search);

        const ticket = urlParams.get('ticket');
        if (ticket) {
            $('#ticket').val(ticket).change();
        }

        const aeroplanStatus = urlParams.get('aeroplanStatus');
        if (aeroplanStatus) {
            $('#aeroplanStatus').val(aeroplanStatus).change();
        }

        const hasBonusPointsPrivilege = urlParams.get('hasBonusPointsPrivilege');
        $('#hasBonusPointsPrivilege').prop( 'checked', hasBonusPointsPrivilege === 'true' );

        const segments = urlParams.get('segments');
        if (segments) {
            $('#segments').val(segments);
        }

        const baseFare = urlParams.get('baseFare');
        if (baseFare) {
            $('#baseFare').val(baseFare);
        }

        const surcharges = urlParams.get('surcharges');
        if (surcharges) {
            $('#surcharges').val(surcharges);
        }

        return segments && baseFare && surcharges;
    }
}(jQuery));
