trumpet-server
==============


POST /trupeteers/{trupeteerId}/trumpets
PUT  /trupeteers/{trupeteerId}/location


GET  /trupeteers/{trupeteerId}/subscritption


Location {latidude, longitude}

---------------------------------------------

Trumpeteer{id, location}
Trumpet{message}
TrumpetEvent{id, trumpeteerId, message, location}

---------------------------------------------

Subscription{trumpeteerId} 
SubscriberLocationResolver -> TrumpetEventListener



