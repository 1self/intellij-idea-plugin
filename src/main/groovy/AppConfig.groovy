def toCompleteVersion() {
    def completeVersion = new StringBuilder()
    completeVersion << configuration.product.version.major
    completeVersion << '.'
    completeVersion << configuration.product.version.minor
    completeVersion << '.'
    completeVersion << configuration.product.version.micro
    completeVersion.toString()
}

configuration {
    product {
        name = '1self-idea-plugin'

        vendor {
            name = '1self.co'
        }
        //JBoss Versioning Convention
        version {
            major = 0  //number related to production release
            minor = 14 //changes or feature additions
            micro = 2  //patches and bug fixes
            previous = 'None'
            complete = toCompleteVersion()
        }

        distribution {
            name = product.name + '-' + toCompleteVersion()
            previousArchiveName = product.name + '-' + product.version.previous

            jar {
                name = product.name
                manifest {
                    details = [
                            'Manifest-Version'      : '1.0',
                            'Sealed'                : 'true',
                            'Specification-Title'   : product.name,
                            'Specification-Version' : toCompleteVersion(),
                            'Specification-Vendor'  : product.vendor.name,
                            'Implementation-Version': toCompleteVersion(),
                            'Implementation-Vendor' : product.vendor.name
                    ]
                }
            }
        }
    }
}
