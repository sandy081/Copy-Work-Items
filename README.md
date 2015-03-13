# Copy-Work-Items
An Eclipse extension for Rational Team Concert (RTC) to copy Work Items across repositories.

## Description

This extension allows you to copy work items from one repository to another repository. This can also be used to do bulk copy across project areas in the same repository. If source and target projects are simple scrum based then this can be a tool of choice for importing.

### Features

* Copying built in attributes like type, summary, description, tags, priority, severity, complexity, creation date, due date, resolution date, estimates and timespent

* Comments, state and resolution details are retained.

* Links, attachments and internal data like rank can be copied

* Backlog items are imported as backlog items.

* Secured by allowing only Admins to operate

### Additional Options

* To replace sensitive information

* Add tags to the copied work items

* Enable or disable copying links and attachments

* Enable or disable copying internal information

### Restrictions

* Users are mapped by user id. Defaults to Unassigned user or Current user (creator, comments)

* Found In, Planned For are set to unassigned

* Retains data only from built in attributes

## Install

Install on top of RTC Eclipse Client using the following update site

https://raw.githubusercontent.com/sandy081/Copy-Work-Items/master/Update-Site/site.xml

**Pre Requisite** - Rational Team Concert 5.0.2 Client

## Usage

* Create a work item query to match the work items for copy

* Right click on the query and select 'Copy Work Items...' option

![Copy Work Items...](https://raw.githubusercontent.com/sandy081/Copy-Work-Items/master/images/Copy%20Work%20Items%20Option.png)

* Select the target repository and project area into which work items are to be imported

* Click finish to copy with default options or Click next to choose options and finish
