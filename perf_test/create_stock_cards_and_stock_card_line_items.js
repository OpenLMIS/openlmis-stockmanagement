var request = require('request');
var baseUrl = process.argv[2];

request.post(
  {
    headers: {
      'content-type': 'application/x-www-form-urlencoded',
      'Authorization': 'Basic dXNlci1jbGllbnQ6Y2hhbmdlbWU='
    },
    url: `${baseUrl}/api/oauth/token?grant_type=password`,
    body: "username=srmanager1&password=password"
  },
  function (error, response, body) {
    var auth_accesstoken = JSON.parse(body).access_token;
    request.get(
      {
        headers: {'content-type': 'application/json;charset=UTF-8'},
        url: `${baseUrl}/api/stockCardSummaries/noCards?access_token=${auth_accesstoken}&facility=176c4276-1fb1-4507-8ad2-cdfba0f47445&program=dce17f2e-af3e-40ad-8e00-3496adef44c3`
      },
      function (error, response, body) {
        var stockCards = JSON.parse(body);
        stockCards.forEach(function (c) {
          console.log(c.orderable.id);
          console.log(c.lot);
        });
        var physicalInventory = {
          "documentNumber": "test123",
          "facilityId": "176c4276-1fb1-4507-8ad2-cdfba0f47445",
          "lineItems": [],
          "programId": "dce17f2e-af3e-40ad-8e00-3496adef44c3",
          "signature": "test"
        };

        stockCards.forEach(function (stockCard) {
          physicalInventory.lineItems.push(
            {
              "orderableId": stockCard.orderable.id,
              "lotId": stockCard.lot ? stockCard.lot.id : null,
              "quantity": 466,
              "occurredDate": new Date("2017-01-01").toJSON()
            });
        });
        request.post(
          {
            headers: {'content-type': 'application/json;charset=UTF-8'},
            url: `${baseUrl}/api/stockEvents?access_token=${auth_accesstoken}`,
            body: JSON.stringify(physicalInventory)
          }, function (error, response, body) {
            console.log(body);
          });
      });
  });
