dataSource {
    pooled = true
    //driverClassName = "org.h2.Driver"
	driverClassName = "com.mysql.jdbc.Driver"
    username = "root"
    password = ""
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = true
    //cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory'
	cache.provider_class = 'net.sf.ehcache.hibernate.EhCacheProvider'
}
// environment specific settings
environments {
    development {
        dataSource {
            dbCreate = "update" // one of 'create', 'create-drop', 'update', 'validate', ''
            //url = "jdbc:h2:mem:devDb;MVCC=TRUE;LOCK_TIMEOUT=10000"
			url = "jdbc:mysql://localhost/eshop1"
        }
    }
    test {
        dataSource {
            dbCreate = "update"
            // url = "jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000"
			url = "jdbc:mysql://localhost/eshop1"
        }
    }
    production {
        dataSource {
            dbCreate = "update"
            // url = "jdbc:h2:prodDb;MVCC=TRUE;LOCK_TIMEOUT=10000"
			driverClassName = "org.postgresql.Driver"
			dialect = org.hibernate.dialect.PostgreSQLDialect
		
			uri = new URI(System.env.DATABASE_URL?:"postgres://test:test@localhost/test")
			
			url = "jdbc:postgresql://"+uri.host+uri.path
			username = uri.userInfo.split(":")[0]
			password = uri.userInfo.split(":")[1]
            pooled = true
            properties {
               maxActive = -1
               minEvictableIdleTimeMillis=1800000
               timeBetweenEvictionRunsMillis=1800000
               numTestsPerEvictionRun=3
               testOnBorrow=true
               testWhileIdle=true
               testOnReturn=true
               validationQuery="SELECT 1"
            }
        }
    }
}
