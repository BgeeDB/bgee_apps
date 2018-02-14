Directory structure:

bgee-applications/      The Java Maven project of Bgee
....bgee-core/          The core layer of Bgee, or "business" layer.
....bgee-dao-api/       The API to use a data source, such as a MySQL database. This API
                        is used by the "bgee-core" and the "bgee-pipeline" modules.
....bgee-dao-sql/       A service provider for the "bgee-dao-api" module, allowing to use
                        a MySQL database as data source.
....bgee-pipeline/      Classes used to generate and insert data into the Bgee database.
                        The jar file generated from this module will be used as part of the pipeline,
                        see pipeline directory description.
....bgee-webapp/        The controller and view layer of the application. This module
                        relies only on the use of the "bgee-core" module. It is not
                        aware of the use under the hood of the other modules.
