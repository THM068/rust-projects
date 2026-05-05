#To trigger fast response in local when checking manually - place tag on scenario you want
Feature: Trigger fast response

  @fast
  Scenario: Trigger fast response journey
    And set the file status to accepted with response

  Scenario: Trigger fast response journey
    And set the file status to rejected with AllErrors

  Scenario: Trigger fast response journey
    And set the file status to problem with SchemaErrors