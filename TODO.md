# Expression comparison page

I noticed a discrepancy between the comparison page and the gene page: for an organ, the gene page uses the max expression score over all stages; for the expression comparison page, it uses an expression score computed (averaged) over all stages.

# ExperimentExpression

* Get rid of all the code using the tables xxxExperimentExpression. For Bgee 15, confidence levels will be based on corrected p-values, not on number of experiments.
* Modify the globalExpression table and related code accordingly.
