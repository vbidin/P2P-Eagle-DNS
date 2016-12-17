example.com.		3600	IN	SOA	ns.example.com. username.example.com. 2007120710 86400 7200 2419200 3600
example.com.		3600	IN	NS	ns.somewhere.example.
example.com.		3600	IN	NS	ns.example.com.
example.com.		3600	IN	MX	10 mail.example.com.
example.com.		3600	IN	MX	20 mail2.example.com.
example.com.		3600	IN	MX	50 mail3.example.com.
example.com.		3600	IN	A	15.15.15.15
example.com.		3600	IN	AAAA	2001:db8:10:0:0:0:0:1
mail.example.com.	3600	IN	A	192.0.2.3
mail2.example.com.	3600	IN	A	192.0.2.4
ns.example.com.		3600	IN	A	192.0.2.2
ns.example.com.		3600	IN	AAAA	2001:db8:10:0:0:0:0:2
www.example.com.	3600	IN	CNAME	example.com.
wwwtest.example.com.	3600	IN	CNAME	www.example.com.
mail3.example.com.	3600	IN	A	192.0.2.5