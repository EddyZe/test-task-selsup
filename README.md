## __Описание__
Реализация класса работы с API честного знака.  
Реализован метод для создания документа ввода товара
в оборот. В качестве аргументов метод принимает
документ в виде Java объекта и подпись в виде строки.  
При вызове метод отправляет запрос на адрес:
```thymeleafurlexpressions
https://ismp.crpt.ru/api/v3/lk/documents/create
````  

В теле запроса передается JSON:  
````JSON
{"description":
{ "participantInn": "string" }, "doc_id": "string", "doc_status": "string",
"doc_type": "LP_INTRODUCE_GOODS", "importRequest": true,
"owner_inn": "string", "participant_inn": "string", "producer_inn":
"string", "production_date": "2020-01-23", "production_type": "string",
"products": [ { "certificate_document": "string",
"certificate_document_date": "2020-01-23",
"certificate_document_number": "string", "owner_inn": "string",
"producer_inn": "string", "production_date": "2020-01-23",
"tnved_code": "string", "uit_code": "string", "uitu_code": "string" } ],
"reg_date": "2020-01-23", "reg_number": "string"}
````

----




## __Запуск программы__

В качестве проверки было создано 10 потоков,
которые пытаются вызвать метод у класса
CrptApi.  
  
При создании класса в конструкторе указаны следующие данные:  

````java
CrptApi crptApi = new CrptApi(TimeUnit.MINUTES, 5);
````

### ___Компиляция___
````
mvn clean install
````

### ___Запуск___
````
java -jar test-task-idea-platform-1.0-SNAPSHOT-jar-with-dependencies.jar
````
