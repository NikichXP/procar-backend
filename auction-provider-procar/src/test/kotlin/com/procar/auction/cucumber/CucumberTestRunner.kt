package com.procar.auction.cucumber

import io.cucumber.junit.Cucumber
import io.cucumber.junit.CucumberOptions
import org.junit.runner.RunWith

@RunWith(Cucumber::class)
@CucumberOptions(
    features = ["classpath:features"],
    glue = ["com.procar.auction.cucumber"],
    plugin = ["pretty", "html:target/cucumber-report", "json:target/cucumber.json"],
    monochrome = true
)
class CucumberTestRunner
