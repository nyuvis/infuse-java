INFUSE
======

A Java implementation of [INFUSE](http://nyuvis.github.io/infuse/).

The project can be build in eclipse or using maven.
A runnable pre-built jar can be found [here](infuse.jar).
When just running the project an example dataset is loaded.
However, the following command line arguments are accepted to load other datasets:

- ```-json <file>``` loads a dataset from a single JSON file.
  The file is assumed to have the structure of [example.json](src/main/resources/data/example.json).

```javascript
{
  "featureSelection": [ // defines the order the feature selection algorithms are displayed
    "FeatureSelectionFisherScore", // the names of the algorithms serve as id
    …
  ],
  "classification": [ // all classification results
    {
      "auc": "0.6322", // the area under curve / displayed quality measure
      "fs": "FeatureSelectionFisherScore", // the feature selection algorithm
      "classifier": "ClassificationTree", // the classifier
      "fold": "0" // the cross-validation fold
    },
    …
  ],
  "features": [ // all features
    {
      "ranks": [ // the ranks of this feature
        {
          "rank": null, // this feature was not picked by the algorithm
          "fs": "FeatureSelectionFisherScore", // the feature selection algorithm
          "fold": "0" // the cross-validation fold
        },
        …
        {
          "rank": "87", // this feature was ranked 87th by the algorithm
          "fs": "FeatureSelectionInformationGain", // the feature selection algorithm
          "fold": "0" // the cross-validation fold
        },
        …
      ],
      "subtype": "ProblemList", // the subtype of the feature
      "name": "feature#0", // the name of the feature
      "type": "DIAGNOSIS" // the type of the feature
    },
    …
  ]
}
```

- ```-old <path> <prefix>``` loads a dataset using the old legacy format.

Optionally, the following argument can be appended in order to anonymize or convert datasets.

- ```-anon <output>``` anonymizes the dataset and saves it to ```output``` in the new JSON format.

Pull requests are highly appreciated.
