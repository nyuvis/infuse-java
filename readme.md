INFUSE
======

A Java implementation of [INFUSE](http://nyuvis.github.io/infuse/).

The project can be build in eclipse or using maven.
A runnable pre-built jar can be found [here](infuse.jar).
When just running the project an example dataset is loaded.
However, the following command line arguments are accepted to load other datasets:

- ```-json <file>``` loads a dataset from a single JSON file.
  The file is assumed to have the structure of [example.json](src/main/resources/data/example.json).
  ```json
  {
    "featureSelection":
    [
      "FeatureSelectionFisherScore",
      ...
    ],
    "classification":
    [
      {
        "auc": "0.6322",
        "fs": "FeatureSelectionFisherScore",
        "classifier": "ClassificationTree",
        "fold": "0"
      },
      ...
    ],
    "features":
    [
      {
        "ranks":
        [
          {
            "rank": null,
            "fs": "FeatureSelectionFisherScore",
            "fold": "0"
          },
          ...
        ],
        "subtype": "ProblemList",
        "name": "feature#0",
        "type": "DIAGNOSIS"
      },
      ...
    ]
  ```

- ```-old <path> <prefix>``` loads a dataset using the old legacy format.

Optionally, the following argument can be appended in order to anonymize or convert datasets.

- ```-anon <output>``` anonymizes the dataset and saves it to ```output``` in the new JSON format.

Pull requests are highly appreciated.
