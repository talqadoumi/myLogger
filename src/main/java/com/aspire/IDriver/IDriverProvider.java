package com.aspire.IDriver;

import io.appium.java_client.AppiumDriver;

public interface IDriverProvider {
	AppiumDriver getCurrentDriver();
	boolean isDriverInitialized();
}
