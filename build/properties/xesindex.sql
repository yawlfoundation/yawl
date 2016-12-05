CREATE INDEX index_LogSpecification ON Logspecification (identifier,specversion,uri);
CREATE INDEX index_LogNetInstance_id ON LogNetInstance (netID);
CREATE INDEX index_LogNetInstance_parentid ON LogNetInstance (parentTaskInstanceID);
CREATE INDEX index_LogTaskInstance_parentid ON LogTaskInstance (parentNetInstanceID);
CREATE INDEX index_LogTask_id ON LogTask (taskID);
CREATE INDEX index_LogEvent_id ON LogEvent (instanceID);
CREATE INDEX index_LogDataItem_id ON LogDataItem (eventID);
CREATE INDEX index_LogDataType_id ON LogDataType (dataTypeID);


