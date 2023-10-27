package com.node.replica.Database;

public enum Response {
    True,
    DatabaseDoesNotExist,
    DataBaseInvalidName,
    NameIsUsed,
    DocumentDoesNotExist,
    PropertyAlreadyExists,
    PropertyDoesNotExist,
    MandatoryProperty,
    ImmutableProperty,
    IndexAlreadyExists,
    IndexDoesNotExist,
    MandatoryIndex,
    //Error
    False,
    Exception,
    Unauthorized,
    Unauthenticated
}
