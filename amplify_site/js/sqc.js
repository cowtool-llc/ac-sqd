/*global SqcCalculator _config*/

var SqcCalculator = window.SqcCalculator || {};

(function flightStatusScopeWrapper($) {
    var authToken;
    var authTokenLoaded = false;
    var isDocReady = false;
    SqcCalculator.authToken.then(function setAuthToken(token) {
        if (token) {
            authToken = token;
        }
        authTokenLoaded = true;
        performOnLoad();
    }).catch(function handleTokenError(error) {
        alert(error);
    });

    function getLambda(token) {
        AWS.config.region = _config.cognito.region;
        const logins = {};
        const activeToken = token || authToken;
        if (activeToken) {
            logins['cognito-idp.' + _config.cognito.region + '.amazonaws.com/' + _config.cognito.userPoolId] = activeToken;
        }
        const credentials = new AWS.CognitoIdentityCredentials({
            IdentityPoolId: _config.cognito.identityPoolId,
            Logins: logins
        });
        AWS.config.credentials = credentials;
        return new AWS.Lambda({ region: 'us-east-1', credentials: credentials });
    }

    async function getFreshAuthToken(forceRefresh = false) {
        if (typeof SqcCalculator.getAuthToken === 'function') {
            try {
                const token = await SqcCalculator.getAuthToken(forceRefresh);
                authToken = token || null;
                updateAuthSection();
            } catch (err) {
                console.warn('Error getting fresh auth token:', err);
            }
        }
        return authToken;
    }

    async function invokeLambda(payload, callback) {
        const freshToken = await getFreshAuthToken();
        const lambda = getLambda(freshToken);
        const params = {
            FunctionName: _config.lambda.functionName,
            InvocationType: 'RequestResponse',
            LogType: 'None',
            Payload: JSON.stringify(Object.assign({}, payload, { authToken: freshToken || "" }))
        };

        return new Promise(function (resolve, reject) {
            lambda.invoke(params, function (err, data) {
                if (typeof callback === 'function') {
                    callback(err, data);
                }
                if (err) {
                    reject(err);
                } else {
                    resolve(data);
                }
            });
        });
    }

    function callLambda(
        ticket,
        aeroplanStatus,
        segments,
        baseFare,
        surcharges
    ) {
        $('#calculateSqc').buttonLoader('start');

        invokeLambda({
            ticket: ticket,
            aeroplanStatus: aeroplanStatus,
            segments: segments,
            baseFare: baseFare,
            surcharges: surcharges
        }, function (err, data) {
            if (err) {
                alert(err);
            } else {
                const response = JSON.parse(data.Payload);
                if (response.errorMessage) {
                    alert(response.errorMessage);
                } else {
                    const results = response.results;
                    populateResults(response.itinerary);
                    populateUrl(ticket, aeroplanStatus, segments, baseFare, surcharges);
                }
            }

            $('#calculateSqc').buttonLoader('stop');
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
                        .attr('align', 'center')
                    )
                    .append($('<td>')
                        .text(segment.origin + '-' + segment.destination)
                        .attr('align', 'center')
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
                        .text(segment.earningResult.eligibleDollars.toLocaleString('en-US'))
                        .attr('align', 'right')
                    )
                    .append($('<td>')
                        .text(segment.earningResult.sqcMultiplier)
                        .attr('align', 'center')
                    )
                    .append($('<td>')
                        .text(segment.earningResult.sqc.toLocaleString('en-US'))
                        .attr('align', 'right')
                    )
                    .append($('<td>')
                        .text(segment.earningResult.basePoints.toLocaleString('en-US'))
                        .attr('align', 'right')
                    )
                    .append($('<td>')
                        .text(segment.earningResult.eliteBonusMultiplier)
                        .attr('align', 'center')
                    )
                    .append($('<td>')
                        .text(segment.earningResult.bonusPoints.toLocaleString('en-US'))
                        .attr('align', 'right')
                    )
                    .append($('<td>')
                        .text(segment.earningResult.totalPoints.toLocaleString('en-US'))
                        .attr('align', 'right')
                    )
                    .append($('<td>')
                        .text(segment.earningResult.lqm.toLocaleString('en-US'))
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
                    .attr('colspan', 3)
                )
                .append($('<td>')
                    .text(itinerary.totalRow.sqc.toLocaleString('en-US'))
                    .attr('align', 'right')
                )
                .append($('<td>')
                    .text(itinerary.totalRow.basePoints.toLocaleString('en-US'))
                    .attr('align', 'right')
                )
                .append($('<td>')
                )
                .append($('<td>')
                    .text(itinerary.totalRow.bonusPoints.toLocaleString('en-US'))
                    .attr('align', 'right')
                )
                .append($('<td>')
                    .text(itinerary.totalRow.totalPoints.toLocaleString('en-US'))
                    .attr('align', 'right')
                )
                .append($('<td>')
                    .text(itinerary.totalRow.lqm.toLocaleString('en-US'))
                    .attr('align', 'right')
                )
            )
    }

    function populateUrl(
        ticket,
        aeroplanStatus,
        segments,
        baseFare,
        surcharges
    ) {
        let queryParams = "?ticket=" + ticket +
            "&aeroplanStatus=" + aeroplanStatus +
            "&segments=" + encodeURIComponent(segments) +
            "&baseFare=" + baseFare +
            "&surcharges=" + surcharges;
        window.history.pushState({ "queryParams": queryParams }, "", queryParams);
    }

    $(function onDocReady() {
        $('#calculateSqc').click(handleRequestClick);

        isDocReady = true;
        performOnLoad();
    });

    function updateAuthSection() {
        if (!authTokenLoaded) {
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
    }

    function performOnLoad() {
        if (!isDocReady || !authTokenLoaded) {
            return;
        }

        updateAuthSection();

        const shouldCalculate = populateFields();

        if (shouldCalculate) {
            performCalculateSqc();
        }
    }

    function performCalculateSqc() {
        const ticket = $('#ticket').val();
        const aeroplanStatus = $('#aeroplanStatus').val();
        const segments = $('#segments').val();
        const baseFare = $('#baseFare').val();
        const surcharges = $('#surcharges').val();
        callLambda(ticket, aeroplanStatus, segments, baseFare, surcharges);
    }

    function handleRequestClick(event) {
        event.preventDefault();
        const form = $('#sqcForm')[0];
        form.reportValidity();
        if (form.checkValidity()) {
            performCalculateSqc();
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
            const $aeroplanStatus = $('#aeroplanStatus');
            const exists = $aeroplanStatus.find('option').filter(function () {
                return $(this).val() === aeroplanStatus;
            }).length > 0;

            if (exists) {
                $aeroplanStatus.val(aeroplanStatus).change();
            } else {
                $aeroplanStatus.val('').change();
            }
        }

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
