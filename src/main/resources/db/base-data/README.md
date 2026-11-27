# Base Data for OpenLMIS Stockmanagement Service

This folder holds the base layer of this service's test data: stock card line item reasons and tags,
valid reason, source and destination assignments, nodes and organizations.

Each data profile (`base-data`, `demo-data`, `functional-test-data`, `edge-case-data`,
`performance-data`) loads `base-data` first, then the layer it names. Rows of a layer refer only to
rows of their own layer or of `base-data`, in this service and in referencedata, whose layers these
follow.
