(function(CometD) {
  const cCometD = CometD.createCometD();
  cCometD.eXoSecret = {
    exoId: eXo.env.portal.userName,
    exoToken: eXo.env.portal.cometdToken,
  };
  cCometD.eXoResubs = [];
  cCometD.eXoPublish = [];
  cCometD.eXoRemoteCalls = [];
  cCometD.autoResubscribe = true;

  cCometD.origConfigure = cCometD.configure;
  cCometD.origHandshake = cCometD.handshake;
  cCometD.origSubscribe = cCometD.subscribe;
  cCometD.origPublish = cCometD.publish;
  cCometD.origRemoteCall = cCometD.remoteCall;

  // Replays calls queued while the client was still handshaking/connecting.
  // Draining the queues (into local copies, then resetting to []) BEFORE replaying
  // is required: replayed subscribe/publish/remoteCall go through the very same
  // overrides below, so if getStatus() is still "handshaking" at replay time, the
  // replay itself re-queues into eXoPublish/eXoRemoteCalls - resetting AFTER the
  // forEach would wipe out that re-queued entry, losing the call forever.
  cCometD.replayPending = function() {
    if (cCometD.eXoPublish.length === 0 && cCometD.eXoRemoteCalls.length === 0) {
      return;
    }
    cCometD.batch(cCometD, function() {
      var pubs = cCometD.eXoPublish;
      var calls = cCometD.eXoRemoteCalls;
      cCometD.eXoPublish = [];
      cCometD.eXoRemoteCalls = [];
      pubs.forEach(elem => cCometD.publish(...elem));
      calls.forEach(elem => cCometD.remoteCall(...elem));
    });
  };

  cCometD.addListener('/meta/handshake', function(message) {
    if (message.successful) {
      //resubcribe after successfull handshake
      cCometD.eXoResubs.forEach(elem => cCometD.subscribe(...elem));
      cCometD.replayPending();
    }
  });

  // getStatus() can still report "handshaking" at the moment /meta/handshake fires
  // (the client hasn't finished the follow-up /meta/connect yet), so a replay
  // attempted from the handshake listener above may re-queue instead of sending.
  // /meta/connect firing successfully guarantees the client is truly "connected",
  // so retry here too - replayPending() is a no-op once the queues are empty.
  cCometD.addListener('/meta/connect', function(message) {
    if (message.successful) {
      cCometD.replayPending();
    }
  });

	cCometD.configure = function(config) {
	  if (config.exoId) {
	    this.eXoSecret = {
        exoId: config.exoId,
        exoToken: config.exoToken
	    }
	  }
	  if (config.autoResubscribe) {
	    this.autoResubscribe = config.autoResubscribe;
	  }
	  cCometD.origConfigure.apply(this, arguments);
	};

	// The server (EXoContinuationBayeux.canHandshake) refuses any handshake that
	// does not carry the eXo secret: inject it here so that a consumer calling
	// handshake() directly (instead of going through subscribe/publish) is
	// accepted too
	cCometD.handshake = function(handshakeProps, handshakeCallback) {
	  if (typeof handshakeProps === 'function') {
	    handshakeCallback = handshakeProps;
	    handshakeProps = undefined;
	  }
	  handshakeProps = {
	    ...cCometD.eXoSecret,
	    ...(handshakeProps || {})
	  };
	  return cCometD.origHandshake.call(this, handshakeProps, handshakeCallback);
	};

	cCometD.subscribe = function(channel, scope, callback, subscribeProps, subscribeCallback) {
	  // Normalize arguments
    if (typeof scope === 'function') {
      subscribeCallback = subscribeProps;
      subscribeProps = callback;
      callback = scope;
      scope = undefined;
    }
    if (typeof subscribeProps === 'function') {
      subscribeCallback = subscribeProps;
      subscribeProps = undefined;
    }
    //Add CometD token
    if (!subscribeProps) {
      subscribeProps = {}
    }
    subscribeProps = {
      ...cCometD.eXoSecret,
      ...subscribeProps
    };

    if (this.autoResubscribe) {
      this.eXoResubs.push([channel, scope, callback, subscribeProps, subscribeCallback]);
    }
    
    if (this.isDisconnected()) {
      this.handshake(subscribeProps);
    } else if(this.getStatus() !== 'handshaking') {
      return cCometD.origSubscribe.call(this, channel, scope, callback, subscribeProps, subscribeCallback);      
    }
	};
	
	cCometD.publish = function(channel, content, publishProps, publishCallback) {
	  if (this.isDisconnected()) {
	    if (!publishProps || typeof publishProps === 'function') {
	      publishProps = {};
	    }
	    //Add eXo token
	    publishProps = {
	      ...cCometD.eXoSecret,
	      ...publishProps
	    };
      this.handshake(publishProps);
    } else if(this.getStatus() === 'handshaking') {
      this.eXoPublish.push(arguments);
    } else {
      return cCometD.origPublish.call(this, channel, content, publishProps, publishCallback);      
    }	  
	};
	
	cCometD.remoteCall = function(target, content, timeout, callback) {
    if (this.isDisconnected()) {
      if (!content || typeof content === 'function') {
        content = {};
      }
      //Add eXo token
      content = {
        ...cCometD.eXoSecret,
        ...content
      };
      this.handshake(content);
    } else if(this.getStatus() === 'handshaking') {
      this.eXoRemoteCalls.push(arguments);
    } else {
      return cCometD.origRemoteCall.call(this, target, content, timeout, callback);      
    }
	};
	
	cCometD.clearResubscriptions = function() {
	  this.eXoResubs = [];
	};

	return cCometD;
})(CometD);