// IMediaTestService.aidl
package com.example.mediatestservice;

// Declare any non-default types here with import statements

interface IMediaTestService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
     void startRecord();
     void stopRecord();
}