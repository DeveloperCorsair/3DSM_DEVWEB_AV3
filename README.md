# automanager – atualização de base (ATVIII)

CRUD completo + HATEOAS para as entidades do diagrama de classes (Figura 1).

## Como rodar

- **MySQL** (padrão do projeto): crie o schema `base` e rode `AutomanagerApplication`
  (`application.properties` já aponta para `localhost:3306/base`, root/root).
- **Sem MySQL (H2 em memória):** rode com o perfil `h2`
  (`-Dspring.profiles.active=h2` ou, no Eclipse, *Run Configurations → Arguments → VM arguments*).

Na subida, o `CommandLineRunner` da equipe popula o banco com uma empresa, um funcionário,
um fornecedor, um cliente, um veículo, mercadoria, serviços e duas vendas.
O ponto de entrada da API é `GET /`, que lista os links de todos os recursos.

## Recursos

| Recurso | Endpoints |
|---|---|
| Empresa | `GET/POST /empresas`, `GET/PUT/DELETE /empresas/{id}` |
| Usuário | `GET/POST /usuarios` (`?perfil=CLIENTE\|FUNCIONARIO\|FORNECEDOR`), `GET/PUT/DELETE /usuarios/{id}` |
| Veículo | `GET/POST /veiculos`, `GET/PUT/DELETE /veiculos/{id}` |
| Mercadoria | `GET/POST /mercadorias`, `GET/PUT/DELETE /mercadorias/{id}` |
| Serviço | `GET/POST /servicos`, `GET/PUT/DELETE /servicos/{id}` |
| Venda | `GET/POST /vendas`, `GET/PUT/DELETE /vendas/{id}` |

### Dados que pertencem a outro recurso (CRUD completo: `GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`)

- `/usuarios/{id}/telefones`, `/emails`, `/documentos`, `/credenciais`
- `/empresas/{id}/telefones`
- Endereço (um por dono): `GET`, `PUT` (cria ou atualiza) e `DELETE` em `/usuarios/{id}/endereco` e `/empresas/{id}/endereco`

### Associações e navegação

- Empresa: `GET /empresas/{id}/usuarios|mercadorias|servicos|vendas`; associar com
  `PUT /empresas/{id}/usuarios/{usuarioId}` (idem mercadorias, servicos, vendas) e desassociar com `DELETE` no mesmo caminho.
- Usuário: `GET /usuarios/{id}/veiculos`, `GET /usuarios/{id}/vendas?como=cliente|funcionario`,
  `GET|PUT|DELETE /usuarios/{id}/mercadorias[/{mercadoriaId}]` (fornecedor).
- Veículo: `GET /veiculos/{id}/vendas`.
- Venda: `GET /vendas/{id}/mercadorias|servicos`, `PUT|DELETE /vendas/{id}/mercadorias/{id}` e `/servicos/{id}`.

Todas as respostas são `EntityModel`/`CollectionModel` (HAL) com links `self`, `todos` e links para os recursos relacionados.

## Exemplos de corpo

```json
// POST /usuarios
{ "nome": "Maria Souza", "perfis": ["CLIENTE"],
  "emails": [{ "endereco": "maria@email.com" }],
  "credenciais": [{ "tipo": "USUARIO_SENHA", "nomeUsuario": "maria", "senha": "123456" }] }

// POST /veiculos
{ "tipo": "SUV", "modelo": "corolla-cross", "placa": "ABC-1234", "proprietario": { "id": 3 } }

// POST /vendas
{ "identificacao": "V-001", "cliente": { "id": 3 }, "funcionario": { "id": 1 },
  "veiculo": { "id": 1 }, "mercadorias": [{ "id": 1 }], "servicos": [{ "id": 1 }, { "id": 2 }] }
```

## Regras e decisões

- Usuário precisa de ao menos um perfil. Venda exige cliente com perfil `CLIENTE`, funcionário com perfil `FUNCIONARIO`, e o veículo (opcional) deve ser do cliente.
- Só fornecedor (`FORNECEDOR`) pode ter mercadorias associadas.
- `PUT` mantém os campos não enviados (exceto `quantidade`/`valor` de mercadoria e `valor` de serviço, que são numéricos primitivos e sempre substituídos).
- Exclusões: usuário, veículo, mercadoria e serviço que já constam em vendas devolvem **409**; fora isso, são desassociados das empresas antes de excluir. Excluir empresa remove também seus serviços e vendas; usuários e mercadorias permanecem, só desassociados.
- Erros devolvem JSON com `status`, `erro` e `mensagem` (404 não encontrado, 400 dados inválidos, 409 conflito/duplicidade).
- A senha da credencial é aceita no `POST/PUT` mas nunca é devolvida.

## Alterações no código da equipe

- `pom.xml`: `spring-boot-starter-hateoas` (traz o web).
- `Credencial`: `@JsonTypeInfo` (campo `tipo`: `USUARIO_SENHA` ou `CODIGO_BARRA`), necessário porque a classe é abstrata.
- `Venda.veiculo` passou de `@OneToOne` para `@ManyToOne` (um veículo tem várias vendas; o `OneToOne` criava restrição de unicidade).
- `Venda.mercadorias`/`servicos` passaram a `@ManyToMany` (o mesmo item do catálogo pode estar em várias vendas).
- `Usuario.veiculos`, `Usuario.vendas` e `Veiculo.vendas` viraram lado inverso (`mappedBy`) e ficam fora do JSON, evitando ciclos e tabelas de junção duplicadas; a navegação é por links.
- `Empresa`: usuários e mercadorias sem `CascadeType.REMOVE`.

## Observação

Senhas continuam em texto puro; isso fica para a "atualização de segurança".
