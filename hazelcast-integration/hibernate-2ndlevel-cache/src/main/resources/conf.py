from lxml import etree
from optparse import OptionParser

CFG_FILE = "src/main/resources/hibernate.cfg.xml"

optparser = OptionParser()

optparser.add_option("-q", "--query-cache",
                     action="store",
                     dest="querycache",
                     help="hibernate.cache.use_query_cache",
                     type="choice",
                     choices=["true", "false"],
                     metavar="true|false",
                     )

optparser.add_option("-m", "--minimal-puts",
                     action="store",
                     dest="minimalputs",
                     help="hibernate.cache.use_minimal_puts",
                     type="choice",
                     choices=["true", "false"],
                     metavar="true|false",
                     )

optparser.add_option("-f", "--factory-class",
                     action="store",
                     type="choice",
                     dest="factoryclass",
                     help="hibernate.cache.region.factory_class",
                     metavar="local|dist",
                     choices=["local", "dist"]
                     )

optparser.add_option("-n", "--native-client",
                     action="store",
                     dest="nativeclient",
                     help="hibernate.cache.hazelcast.use_native_client",
                     type="choice",
                     choices=["true", "false"],
                     metavar="true|false",
                     )
optparser.add_option("-i", "--hosts",
                     action="store",
                     type="string",
                     dest="hosts",
                     help="hibernate.cache.hazelcast.native_client_hosts",
                     metavar="IP(:PORT)",
                     )

optparser.add_option("-g", "--group",
                     action="store",
                     type="string",
                     dest="group",
                     help="hibernate.cache.hazelcast.native_client_group",
                     )

optparser.add_option("-p", "--password",
                     action="store",
                     type="string",
                     dest="password",
                     help="hibernate.cache.hazelcast.native_client_password",
                     )

(options, args) = optparser.parse_args()

xmlparser = etree.XMLParser(remove_comments=False, encoding="UTF-8")
tree = etree.parse(CFG_FILE, parser=xmlparser)
root = tree.getroot()
factorynode = root.find("session-factory")

for child in factorynode:
    if not child.attrib or not child.attrib.get("name"):
        pass
    elif child.attrib["name"] == "hibernate.cache.use_query_cache":
        querycache = child
    elif child.attrib["name"] == "hibernate.cache.use_minimal_puts":
        minimalputs = child
    elif child.attrib["name"] == "hibernate.cache.region.factory_class":
        factoryclass = child
    elif child.attrib["name"] == "hibernate.cache.hazelcast.use_native_client":
        nativeclient = child
    elif child.attrib["name"] == "hibernate.cache.hazelcast.native_client_hosts":
        hosts = child
    elif child.attrib["name"] == "hibernate.cache.hazelcast.native_client_group":
        group = child
    elif child.attrib["name"] == "hibernate.cache.hazelcast.native_client_password":
        password = child

if options.querycache:
    querycache.text = options.querycache
if options.minimalputs:
    minimalputs.text = options.minimalputs
if options.factoryclass:
    if options.factoryclass == "dist":
        factoryclass.text = "com.hazelcast.hibernate.HazelcastCacheRegionFactory"
    else:
        factoryclass.text = "com.hazelcast.hibernate.HazelcastLocalCacheRegionFactory"
if options.nativeclient:
    nativeclient.text = options.nativeclient
    if options.nativeclient == "true":
        if options.hosts and options.group and options.password:
            hosts.text = options.hosts
            group.text = options.group
            password.text = options.password
        else:
            print "You haven't stated hosts, group and password"
            exit(1)
if options.hosts:
    hosts.text = options.hosts
if options.group:
    group.text = options.group
if options.password:
    password.text = options.password

tree.write(CFG_FILE)
