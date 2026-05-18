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
  cCometD.origSubscribe = cCometD.subscribe;
  cCometD.origPublish = cCometD.publish;
  cCometD.origRemoteCall = cCometD.remoteCall;

  cCometD.addListener('/meta/handshake', function(message) {
    if (message.successful) {
      //start a batch
      cCometD.batch(cCometD, function() {
        //resubcribe after successfull handshake
        cCometD.eXoResubs.forEach(elem => cCometD.subscribe(...elem));
        //publish
        cCometD.eXoPublish.forEach(elem => cCometD.publish(...elem));
        cCometD.eXoPublish = [];
        //remoteCall
        cCometD.eXoRemoteCalls.forEach(elem => cCometD.remoteCall(...elem));
        cCometD.eXoRemoteCalls = [];
      });
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
	    if (!publishProps || _isFunction(publishProps))
	    {
	      publishProps = {};
	    }

	    //Add eXo token
	    publishProps = $.extend({}, cCometD.eXoSecret, publishProps);
      this.handshake(publishProps);
    } else if(this.getStatus() === 'handshaking') {
      this.eXoPublish.push(arguments);
    } else {
      return cCometD.origPublish.call(this, channel, content, publishProps, publishCallback);      
    }	  
	};
	
	cCometD.remoteCall = function(target, content, timeout, callback) {    
    if (this.isDisconnected()) {
      if (!content || _isFunction(content))
      {
        content = {};
      }
      
      //Add eXo token
      content = $.extend({}, cCometD.eXoSecret, content);
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