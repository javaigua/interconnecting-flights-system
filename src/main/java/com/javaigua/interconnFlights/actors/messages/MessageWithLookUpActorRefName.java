package com.javaigua.interconnFlights.actors.messages;

/**
 * Implementing this interface allows an actor get a value that should be associated with the instance of another actor.
 */
public interface MessageWithLookUpActorRefName {

  /**
   * Provides a look up value based on the properties of this message instance.
   * @return
   */
  String getLookUpName();
}
