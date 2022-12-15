package org.exoplatform.commons.api.notification.channel;

import java.util.Collections;
import java.util.List;

import org.exoplatform.commons.api.notification.channel.template.TemplateProvider;
import org.exoplatform.commons.api.notification.lifecycle.AbstractNotificationLifecycle;
import org.exoplatform.commons.api.notification.model.ChannelKey;

public interface ChannelManager {

  /**
   * Register new channel
   * 
   * @param channel
   */
  void register(AbstractChannel channel);

  /**
   * Unregister the specified channel
   * 
   * @param channel
   */
  void unregister(AbstractChannel channel);

  /**
   * Register the Template provider
   * 
   * @param provider
   */
  void registerTemplateProvider(TemplateProvider provider);
  
  /**
   * Register and override the Template provider
   * 
   * @param provider
   */
  void registerOverrideTemplateProvider(TemplateProvider provider);

  /**
   * Gets the channel by the specified key
   * @param key the channel key
   * @return
   */
  AbstractChannel getChannel(ChannelKey key);

  /**
   * Gets list of the channels
   * @return
   */
  List<AbstractChannel> getChannels();

  /**
   * This will use {@link AbstractChannel#isDefaultChannel()} to determine
   * whether the channel will be enabled for plugins by default or not
   * 
   * @return {@link List} of active channels for all plugins by default.
   */
  default List<AbstractChannel> getDefaultChannels() {
    return getChannels();
  }

  /**
   * This will use {@link AbstractChannel#isDefaultChannel()} to determine
   * whether the channel is specific for some plugins or not
   * 
   * @return {@link List} of active channels for specific plugins only.
   */
  default List<AbstractChannel> getSpecificChannels() {
    return Collections.emptyList();
  }

  /**
   * Gets the lifecycle by the ChannelKey
   * @param key
   * @return
   */
  AbstractNotificationLifecycle getLifecycle(ChannelKey key);

  /**
   * Gets size of channels has been registered
   * 
   * @return
   */
  int sizeChannels();

}
