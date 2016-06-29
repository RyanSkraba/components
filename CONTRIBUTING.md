# Contributing to Talend Components

:+1::tada: First off, thanks for taking the time to contribute! :tada::+1:

The following is a set of guidelines for contributing to Talend Components 

[How Can I Contribute?](#how-can-i-contribute)
  * [Pull Requests](#pull-requests)

[Styleguides](#styleguides)
  * See the [tooling](/tooling/) folder.

## How Can I Contribute?
### Pull Requests
Here is the workflow for pull requests.

1. Submit your pull request.
   => our jenkins should kick 2 builds, one on the branch you have created and one with the branch merged into master.
   So you should see this nice image on github after a while.
   
   ![compProp](https://github.com/Talend/components/wiki/images/Pull_Requests_all_ok_Talend_components.png)
   
   The first green check indicates that the branch build is fine, the label "Merged build passed" indicates that the merge to master build is fine. 
2. If you receive an email notification that there is a build issue please fix it. No PR will be reviewed if the build has failed.
3. The components owners will then identify who is going to do the review and then assign it. The reviewer will recieve an automatic mail notification.
4. Once the reviewer starts the review, he/she should set the label *Reviewing*.
5. Once the review is done, the reviewer has 3 alternatives states:
  * **Can be merged**, the reviewer is happy with the PR so the PR can be merged. The reviewer should set the label *Can be Merged* and then Merge and Squash the PR.
  * **Can be merged after rework**, The review is fine but requires some minor (cosmetic) adjustments that do not need to be reviewed again. The PR is then assigned back to the PR's creator for some minor work. Once the work is done the creator can directly Merge and Squash it without the need of any review. If the creator does not have the right to do so, it will assign it back to the Reviewer after rework so that it can be merged.
  * **Rework Required**, the review added some comments that need work to be done and another review is required after. The reviewer assigns it to the PR creator.
6. if **Rework Required** once assigne to the PR creator, he/she has to make the required changes and then assign it back to the initial Reviewer. Then back to step 4.
