Configuration
-------------
When bootstrapping tha application will scan for configurations in the order described below.
Properties defined in one file has precedence over properties defined later in the list of sources.

    file:trumpet.config
    file:~/.trumpet.config
    file:/etc/trumpet.config

Commandline options
-------------------
Default port: 9191 
Default host: 0.0.0.0

    java -Dserver.http.port=9999 -Dserver.http.host=localhost -Dgcm.apiKey=GCM_API_KEY -jar trumpet-server-1.0.0-shadow.jar


ENTRY POINT
-----------

    curl "http://localhost:9191/api/" 

Response: 200

 ```javascript
{
   "_links": {
         "create-trumpeteer": { "href" : "http://localhost:9191/api/trumpeteers/" },
    }
}
```

CREATE TRUMPETEER
-----------
    curl -X POST --data "type=sse&registrationID=32314234234&latitude=55.583985&longitude=12.957578&accuracy=100" http://localhost:9191/api/trumpeteers

The form parameter type can be : sse | gcm
Only type 'sse' will produce "sse-subscribe" link
Only type 'gcm' will have a registrationID parameter 
"self" link supports GET and DELETE
Response: Content-type: application/json

```javascript
{
    "type": "sse",
    "trumpeteerId" : 1,
    "_links": {
        "sse-subscribe": { "href" : "http://localhost:9191/api/trumpeteers/1/subscription/sse" },
        "self": { "href" : "http://localhost:9191/api/trumpeteers/1" },         
        "update-location": { "href" : "http://localhost:9191/api/trumpeteers/1/location" },
        "trumpet": { "href" : "http://localhost:9191/api/trumpeteers/1/trumpet" },
    }
}
```

DELETE TRUMPETEER
-----------------
    curl -X DELETE http://localhost:9191/api/trumpeteers/1

Unsubscribe the supplied trumpeteers

ME
---

    curl -X GET "Accept: application/json" http://localhost:9191/api/trumpeteers/1?distance=200
Response: 
```javascript
{
    "trumpeteersInRange": 12 
}
```

LOCATION
--------

    curl -X PUT -H --data "latitude=55.583985&longitude=12.957578&accuracy=10" http://localhost:9191/api/trumpeteers/1/location
Response: 200 no content

TRUMPET
-------

    curl -X POST --data "message=This is my first trumpet&distance=200&topic=foo" http://localhost:9191/api/trumpeteers/1/trumpet
The form parameter distance is optional
Response: Content-type: application/json
```javascript
{
    "trumpetId": "121212121" 
}
```

SUBSCRIPTION (SSE)
------------------
1. Open EventSource to entry point link with rel 'subscription' returned when POSTING to ep:subscriptions  
2. Message to subscribe to is 'trumpet'
3. Message format is:
```javascript
{
   "id": "1", 
   "timestamp": 121212122, 
   "message": "foo", 
   "topic": "bar", 
   "distanceFromSource": 240,
   "accuracy": 10,
   "sentByMe": false
}
```