# Copy-Work-Items
An Eclipse extension for Rational Team Concert (RTC) to copy Work Items across repositories.

## Description

This extension allows you to copy work items from one repository to another repository. This can also be used to do bulk copy across project areas in the same repository. If source and target projects are simple scrum based then this can be a tool of choice for importing.


This retains data only from built in attributes.

* Type, Summary, Description, Priority, Severity, Tags, Category

* All comments are retained

* Backlog items are mapped and imported to target backlog

* States and resolutions are copied

Restrictions

* Users are mapped by user id. Defaults to Unassigned user or Current user (creator, comments)

* Found In, Planned For are set to unassigned


It provides following options while copying

* To replace sensitive information

* Add tags to the copied work items

* Copy the links of the work items

* Copy attachments

* Copy or retain internal information like ranking

## Install and Usage

Install it on top of RTC Eclipse Client using the following update site link:

https://raw.githubusercontent.com/sandy081/Copy-Work-Items/master/Update-Site/site.xml

**Pre Requisite** - Rational Team Concert 5.0.2 Client
