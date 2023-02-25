# Angeloni - CSC Server

Servidor de integração da API do TempoCerto e a API do Angeloni.

## Tecnologias

* Java/JDK 8
* Spring Boot 2.x
* Lombok
* Maven

## Instruções

### Alterar as configurações

Todas as configurações estão disponíveis no arquivo `src/main/resources/application.yml` e podem 
ser [alteradas externamente via parâmetro ou variável de ambiente](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html).

Exemplo configuração externa:
`java -jar target/csc-server.jar --spring.config.location=classpath:/application.yml,file:/etc/config.yml`

_Nota: Arquivos de logs são gerados no diretório `logs` e automaticamente compactados todos os dias._

### Construir e testar

`./mvnw clean package`

_Nota: Adicione `-DskipTests` para ignorar a fase de testes._

### Iniciar o servidor de integração

`java -jar target/csc-server.jar --spring.profiles.active=prod`

_Nota: O profile de produção está sendo ativado no comando acima._

### Acessar a interface web do Swagger (API Docs)

http://localhost:8081/swagger-ui/

### Testar logger syslog com Docker

`docker run --rm -p 514:514/udp jumanjiman/rsyslog`

## Integrar métricas com Prometheus

### Datasource
http://localhost:8081/actuator/prometheus

### Testar Prometheus com Docker

1. `docker-compose build`
2. `docker-compose up grafana`
3. http://localhost:3000 (admin/admin)

## RabbitMQ

1. `docker-compose build`
2. `docker-compose up rabbitmq`
3. http://localhost:15672 (admin/admin)

### Referências:
- https://www.rabbitmq.com/admin-guide.html
- https://imasters.com.br/devsecops/clusterizando-rabbitmq-com-docker-compose
