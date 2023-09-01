# Basic-functions

This project is a basic example which uses:

- Sample functions from Java internal
- Sample functions from the Quarkus application.
- Sample OpenAPI functions from GitHub using custom REST to be able to get the token from the user.
- A way to validate data using dataInputstream, so input validation can be done


## How to run:

Using knative workflow tool:

```
kn workflow quarkus run
```

And the application will run using docker in `localhost:8080`, the workflow can
be executed running the following command:

The first time using this, it'll return some json, and a branch will be created
```
curl \
    -H 'Content-Type:application/json' \
    -H 'Accept:application/json' \
    "http://localhost:8080/hello" \
    -d '{"github_token": "TOKEN_TO_BE_USED","branch": "newBranch", "org": "eloycoto", "repo": "dotfiles", "base_branch": "master" }' | jq .
```

Response:
```
{
  "id": "9d523585-a874-406e-ae8f-010901031c8b",
  "workflowdata": {
    "github_token": "TOKEN_TO_BE_USED",
    "branch": "newBranch",
    "org": "eloycoto",
    "repo": "dotfiles",
    "base_branch": "master",
    "message": "Hello World",
    "ref": "refs/heads/newBranch",
    "node_id": "MDM6UmVmOTg2NzA0MzpyZWZzL2hlYWRzL3Rlc3RUZXN0LWpvLW1lcmRh",
    "url": "https://api.github.com/repos/eloycoto/dotfiles/git/refs/heads/testTest-jo-merda",
    "object": {
      "sha": "c4c7f4c46fff1ef11cd46ce6782f4bcbecdbf1b9",
      "type": "commit",
      "url": "https://api.github.com/repos/eloycoto/dotfiles/git/commits/c4c7f4c46fff1ef11cd46ce6782f4bcbecdbf1b9"
    }
  }
}
```

