INSERT INTO job_opportunities (id, title, company_name, location, employment_type, description, job_url, source, publication_date, recruiter_name, match_score, status) VALUES
(uuid_generate_v4(), 'Senior Java Backend Developer', 'SAP SE', 'Berlin, Germany', 'FULL_TIME',
 'We are looking for an experienced Java Backend Developer to join our cloud platform team. You will design and implement scalable microservices using Spring Boot, Kubernetes, and AWS. Strong experience with Java 17+, Spring ecosystem, and REST API design required. Join 100,000+ colleagues shaping the future of enterprise software.',
 'https://jobs.sap.com/job/senior-java-backend-berlin', 'MOCK', CURRENT_DATE - INTERVAL '1 day', 'Klaus Mueller', 94, 'NEW'),

(uuid_generate_v4(), 'Java Software Engineer', 'Deutsche Telekom', 'Bonn, Germany', 'FULL_TIME',
 'Join our innovation team building next-generation telecom solutions. Tech stack: Java 21, Spring Boot 3, PostgreSQL, Docker, Kafka. You will work in an agile environment delivering high-availability services for millions of users. Strong Java background and microservices experience required.',
 'https://telekom.com/careers/java-software-engineer', 'MOCK', CURRENT_DATE - INTERVAL '2 days', 'Anna Schmidt', 91, 'NEW'),

(uuid_generate_v4(), 'Data Engineer', 'BNP Paribas', 'Paris, France', 'FULL_TIME',
 'Looking for a Data Engineer to build and maintain our data pipelines at one of Europe''s largest banks. Experience with Python, Apache Spark, dbt, and cloud platforms (Azure/AWS). Knowledge of data lakehouse architecture is a plus. Hybrid work arrangement available in Paris.',
 'https://bnpparibas.com/jobs/data-engineer-paris', 'MOCK', CURRENT_DATE - INTERVAL '1 day', 'Marie Dupont', 88, 'NEW'),

(uuid_generate_v4(), 'Senior Data Analyst', 'Zalando', 'Berlin, Germany', 'FULL_TIME',
 'Zalando is hiring a Senior Data Analyst to drive business insights for Europe''s largest fashion marketplace. You will work with large datasets, build dashboards, and present actionable findings to senior stakeholders. SQL, Python, and Tableau/Power BI required. Great engineering culture.',
 'https://zalando.com/jobs/senior-data-analyst', 'MOCK', CURRENT_DATE - INTERVAL '3 days', 'Lena Fischer', 87, 'NEW'),

(uuid_generate_v4(), 'Power BI Developer', 'Volkswagen Group', 'Wolfsburg, Germany', 'FULL_TIME',
 'Join VW Group IT as a Power BI Developer. Build enterprise-grade reports and dashboards for global manufacturing KPIs and C-suite executives. DAX, Power Query, and data modeling experience required. Experience with Azure Synapse Analytics is a strong plus.',
 'https://vwgroup.com/careers/power-bi-developer', 'MOCK', CURRENT_DATE - INTERVAL '4 days', 'Hans Weber', 85, 'NEW'),

(uuid_generate_v4(), 'Remote Java Developer', 'Thoughtworks', 'Remote (Europe)', 'FULL_TIME',
 'Thoughtworks is hiring remote Java developers to work on transformative client projects across industries including finance, healthcare, and retail. Java 21, Spring Boot, TDD, and an agile mindset are required. Fully remote with occasional travel for client engagements.',
 'https://thoughtworks.com/jobs/remote-java-developer-europe', 'MOCK', CURRENT_DATE - INTERVAL '2 days', 'Sarah Johnson', 92, 'NEW'),

(uuid_generate_v4(), 'Business Intelligence Analyst', 'Airbus', 'Toulouse, France', 'FULL_TIME',
 'Airbus is seeking a BI Analyst to support aerospace manufacturing operations analytics. Power BI, SQL Server, and SSAS experience required. You will build self-service analytics solutions for operations teams worldwide. Aviation industry knowledge is a plus. Hybrid work available.',
 'https://airbus.com/careers/bi-analyst-toulouse', 'MOCK', CURRENT_DATE - INTERVAL '5 days', 'Pierre Martin', 83, 'NEW'),

(uuid_generate_v4(), 'Senior Data Engineer', 'N26', 'Remote (Europe)', 'FULL_TIME',
 'N26, Europe''s leading mobile bank, is looking for a Senior Data Engineer to build real-time data pipelines processing millions of daily transactions. Kafka, Apache Spark, Scala/Python, and Databricks required. Fully remote within Europe. Join a team that values ownership and autonomy.',
 'https://n26.com/careers/senior-data-engineer', 'MOCK', CURRENT_DATE - INTERVAL '1 day', 'Lisa Becker', 89, 'NEW'),

(uuid_generate_v4(), 'Java Backend Engineer', 'Amadeus', 'Nice, France', 'FULL_TIME',
 'Amadeus, global leader in travel technology powering 24/7 flight bookings, seeks a Java Backend Engineer. Build and maintain high-throughput APIs critical to global aviation. Java 21, Spring Boot, Kafka, and Cassandra experience preferred. On-site or hybrid in Nice, French Riviera.',
 'https://amadeus.com/jobs/java-backend-engineer-nice', 'MOCK', CURRENT_DATE - INTERVAL '6 days', 'Sophie Laurent', 86, 'NEW'),

(uuid_generate_v4(), 'Data Analyst - Fintech', 'Qonto', 'Paris, France', 'FULL_TIME',
 'Qonto, Europe''s leading business finance solution, is hiring a Data Analyst. Analyze product metrics, build self-serve analytics tools, and collaborate with product and engineering teams. SQL, dbt, and Metabase/Looker required. Fast-paced startup culture, hybrid work in Paris.',
 'https://qonto.com/jobs/data-analyst-paris', 'MOCK', CURRENT_DATE - INTERVAL '3 days', 'Thomas Bernard', 84, 'NEW'),

(uuid_generate_v4(), 'Full Stack Java Developer', 'OVHcloud', 'Roubaix, France', 'FULL_TIME',
 'OVHcloud, Europe''s largest cloud provider, seeks a Full Stack Java Developer for its infrastructure management platform. Java/Spring Boot backend with Angular frontend. Kubernetes, CI/CD pipelines, and a strong DevOps culture. Hybrid or fully remote within France.',
 'https://ovhcloud.com/careers/fullstack-java-developer', 'MOCK', CURRENT_DATE - INTERVAL '7 days', 'Isabelle Moreau', 80, 'NEW'),

(uuid_generate_v4(), 'Senior Power BI Developer', 'Siemens', 'Munich, Germany', 'FULL_TIME',
 'Siemens Digital Industries is looking for a Senior Power BI Developer to design enterprise analytics solutions for industrial IoT and manufacturing data. Power BI Premium, Azure Synapse, SQL, and advanced DAX knowledge required. Strong analytical skills and stakeholder management experience essential.',
 'https://siemens.com/jobs/senior-power-bi-developer-munich', 'MOCK', CURRENT_DATE - INTERVAL '2 days', 'Michael Braun', 82, 'NEW'),

(uuid_generate_v4(), 'Backend Java Developer', 'Capgemini', 'Remote (EU)', 'CONTRACT',
 'Capgemini is looking for a contract Java Backend Developer for a 6-month engagement working on a financial services platform modernization. Spring Boot 3, microservices architecture, AWS, and PostgreSQL required. Fully remote. EU work authorization required.',
 'https://capgemini.com/jobs/java-backend-contractor-eu', 'MOCK', CURRENT_DATE - INTERVAL '4 days', 'David Müller', 78, 'NEW'),

(uuid_generate_v4(), 'Junior Data Engineer', 'Deezer', 'Paris, France', 'FULL_TIME',
 'Deezer, global music streaming platform with 16M+ users, seeks a Junior Data Engineer to join the data platform team. Python, Apache Airflow, BigQuery, and dbt experience required. Great opportunity for growth in a dynamic tech company with real impact on the product experience.',
 'https://deezer.com/jobs/junior-data-engineer', 'MOCK', CURRENT_DATE - INTERVAL '8 days', 'Camille Petit', 76, 'NEW'),

(uuid_generate_v4(), 'Software Engineer - Data Platform', 'HelloFresh', 'Berlin, Germany', 'FULL_TIME',
 'HelloFresh is seeking a Software Engineer for the Data Platform team powering global meal-kit operations in 18 countries. Build internal tooling and high-throughput data pipelines. Python, Apache Spark, Kafka, and Kubernetes experience required. Hybrid work arrangement in Berlin.',
 'https://hellofresh.com/careers/software-engineer-data-platform', 'MOCK', CURRENT_DATE - INTERVAL '5 days', 'Julia Hoffmann', 81, 'NEW');
