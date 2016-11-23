#! /usr/bin/python
# Fetch and email some Jira statistics.
# Copyright 2016 Indiana University
# Mark H. Wood, IUPUI University Library, 16-Nov-2016

"""
Fetch and email a snapshot of Jira statistics.

If Jira credentials are needed, they are fetched from an INI-style file
'~/.credentials' with the Jira hostname as section and keys 'username' and
'password'.  Currently this seems un-needed, so that code is commented out.
Eh, netrc.netrc() is broken w.r.t. entries with no 'password' and I have some.
"""

JIRA_HOST = 'jira.duraspace.org'
SERVICE='https://' + JIRA_HOST + '/rest/api/2/search'

#import configparser
import json
#import os.path
import urllib.parse
import urllib.request

def main():
#    config = configparser.ConfigParser()
#    config.read(os.path.expanduser('~/.credentials'))
#    USERNAME = config[JIRA_HOST]['username']
#    PASSWORD = config[JIRA_HOST]['password']

    auth_handler = urllib.request.HTTPBasicAuthHandler()
#    auth_handler.add_password(realm='PDQ Application',
#                            uri=SERVICE,
#                            user=USERNAME,
#                            passwd=PASSWORD)
    opener = urllib.request.build_opener(auth_handler)
    urllib.request.install_opener(opener)

    REQ_HEADERS = {}
#    REQ_HEADERS['Authorization'] = (USERNAME + ':' + PASSWORD).encode('utf-8')
    REQ_HEADERS['ContentType'] = 'application/json'

    url = SERVICE + '?' + urllib.parse.urlencode(
        {'jql': 'project=DSpace AND status=Received ORDER BY key ASC, priority DESC', \
        'fields': 'key'})
    request = urllib.request.Request(url=url, headers=REQ_HEADERS);
    with urllib.request.urlopen(request) as response:
        body = str(response.read(), 'utf-8')
        issues = json.loads(body)
    print('There are {:d} Received issues.  Get to work!'.format(issues['total']))

### START HERE
if __name__ == '__main__':
    main()
