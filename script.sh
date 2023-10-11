#bin/bash
echo "version: '3'" > docker-compose.yml
echo "services:" >> docker-compose.yml

echo "  bootstrapping:" >> docker-compose.yml
echo "   build: ." >> docker-compose.yml
echo "   ports:" >> docker-compose.yml
echo "    - \"8080:8080\"" >> docker-compose.yml


for i in $(seq 1 10); do
	port=$((8080 + i))
	echo "  node_${i}:" >> docker-compose.yml
	echo "    build: ./replica" >> docker-compose.yml 
 	echo "    ports:" >> docker-compose.yml
	echo "    - \"${port}:${port}\"" >> docker-compose.yml
	echo "	  depends_on:" >> docker-compose.yml
	echo "	    - boostrapping" >> docker-compose.yml
	echo "	  environment:" >> docker-compose.yml
	echo "	    BOOTSTRAPPING_PORT : 8080" >> docker-compose.yml
	echo "      MY_PORT: ${port}" >> docker-compose.yml
done