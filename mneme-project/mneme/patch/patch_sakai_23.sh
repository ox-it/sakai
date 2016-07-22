#!/bin/sh
cd ../../
#SAK-7151
cd util
svn merge -r18134:18135 https://source.sakaiproject.org/svn/util/trunk
#A    util-api/api/src/java/org/sakaiproject/i18n
#A    util-api/api/src/java/org/sakaiproject/i18n/InternationalizedMessages.java
#U    util-util/util/src/java/org/sakaiproject/util/ResourceLoader.java
#
#SAK-7269
svn merge -r18444:18445 https://source.sakaiproject.org/svn/util/trunk
#U    util-util/util/src/java/org/sakaiproject/util/StringUtil.java
#
#SAK-7611
svn merge -r20267:20268 https://source.sakaiproject.org/svn/util/trunk
#G    util-util/util/src/java/org/sakaiproject/util/StringUtil.java
svn merge -r20280:20281 https://source.sakaiproject.org/svn/util/trunk
#G    util-util/util/src/java/org/sakaiproject/util/StringUtil.java
#
#SAK-7309
cd ../authz
svn merge -r18599:18600 https://source.sakaiproject.org/svn/authz/trunk
#U    authz-api/api/src/java/org/sakaiproject/authz/api/SecurityService.java
#U    authz-api/api/src/java/org/sakaiproject/authz/cover/SecurityService.java
#U    authz-impl/impl/src/java/org/sakaiproject/authz/impl/SakaiSecurity.java
#
#SAK-7150
cd ../db
svn merge -r18135:18136 https://source.sakaiproject.org/svn/db/trunk
#U    db-impl/impl/src/java/org/sakaiproject/db/impl/BasicSqlService.java
#U    db-api/api/src/java/org/sakaiproject/db/api/SqlService.java
#U    db-api/api/src/java/org/sakaiproject/db/cover/SqlService.java
#
#SAK-7597
svn merge -r20250:20251 https://source.sakaiproject.org/svn/db/trunk
#G    db-impl/impl/src/java/org/sakaiproject/db/impl/BasicSqlService.java
#G    db-api/api/src/java/org/sakaiproject/db/api/SqlService.java
#
#SAK-8558 (TX support in SqlService)
svn merge -r22825:22826 https://source.sakaiproject.org/svn/db/trunk
#U    db-util/storage/src/java/org/sakaiproject/util/BaseDbFlatStorage.java
#U    db-impl/impl/src/java/org/sakaiproject/db/impl/SqlServiceTest.java
#G    db-impl/impl/src/java/org/sakaiproject/db/impl/BasicSqlService.java
#U    db-impl/pack/src/webapp/WEB-INF/components.xml
#G    db-api/api/src/java/org/sakaiproject/db/api/SqlService.java
#A    db-api/api/src/java/org/sakaiproject/db/api/SqlServiceDeadlockException.java
#A    db-api/api/src/java/org/sakaiproject/db/api/SqlServiceUniqueViolationException.java
#G    db-api/api/src/java/org/sakaiproject/db/cover/SqlService.java
svn revert db-util/storage/src/java/org/sakaiproject/util/BaseDbFlatStorage.java
#Reverted 'db-util/storage/src/java/org/sakaiproject/util/BaseDbFlatStorage.java'
#
# SAK-9830 (Float support in SqlService for prepared statements)
svn merge -r29864:29865 https://source.sakaiproject.org/svn/db/trunk
#G    db-impl/impl/src/java/org/sakaiproject/db/impl/BasicSqlService.java
#
#SAK-8624
cd ../reference
svn merge -r21831:21832 https://source.sakaiproject.org/svn/reference/trunk
#U    library/src/webapp/js/headscripts.js
svn merge -r22556:22557 https://source.sakaiproject.org/svn/reference/trunk
#G    library/src/webapp/js/headscripts.js
#
#SAK-9361
svn merge -r28500:28501 https://source.sakaiproject.org/svn/reference/trunk
#G    library/src/webapp/js/headscripts.js
#
#SAK-7154
cd ../site
svn merge -r18632:18633 https://source.sakaiproject.org/svn/site/trunk
#U    site-impl/impl/src/java/org/sakaiproject/site/impl/ResourceVector.java
#U    site-api/api/src/java/org/sakaiproject/site/api/SitePage.java
#U    site-impl/impl/src/java/org/sakaiproject/site/impl/BaseSitePage.java
cd ..
#

