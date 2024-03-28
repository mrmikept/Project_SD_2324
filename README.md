# Sistema Distribuídos - Projeto Cloud Computing com Function-as-a-Service (FaaS)

Repositório do trabalho prático da UC de Sistemas Distribuídos (SD) da Universidade do Minho do ano letivo 2023/24.

## Objetivo

Este projeto tem como objetivo a criação e implementação de um serviço de computação em nuvem utilizando o modelo Function-as-a-Service (FaaS).

## Funcionalidades

- **Cliente-Servidor**: Utiliza-se um cliente em uma máquina local para enviar o código de uma tarefa de computação a ser executada em um servidor.
- **Execução Sob Demanda**: O servidor executa a tarefa assim que houver disponibilidade, retornando o resultado ao cliente.
- **Limitação de Recursos**: Assume-se que o fator limitante nos servidores é a memória disponível.
- **Simplicidade de Dados**: Tanto o código da tarefa a ser executada quanto o seu resultado são representados por arrays de bytes simples (`byte[]`).
- **Execução da Tarefa**: A execução é realizada utilizando a função `JobFunction.execute()` fornecida no arquivo `sd23.jar`.
- **Gerenciamento de Filas**: O serviço mantém uma fila de espera de tarefas para execução, garantindo uma boa utilização dos recursos disponíveis, sem ultrapassar o máximo de memória disponível.

## Funcionamento

O cliente envia uma tarefa de computação para o servidor, que o coloca numa fila de espera para execução. Assim que recursos (principalmente memória) estiverem disponíveis, o servidor executa a tarefa e retorna o resultado ao cliente. O serviço é projetado para otimizar o uso dos recursos, evitando que os pedidos em execução concorrente ultrapassem o limite máximo disponível de memória.

## Dependências

- Este projeto requer o arquivo `sd23.jar` para a execução das tarefas.

## Autores
<div align="center">

|    **Nome**    | **Número** |
|:--------------:|:----------:|
| [Lucas Oliveira](https://github.com/LucasOli20) |   A98695   |
|   [Mike Pinto](https://github.com/mrmikept)   |   A89292   |
|  [Rafael Gomes](https://github.com/RafaGomes1) |   A96208   |
| [Tiago Carneiro](https://github.com/Tiago5Carneiro) | A93207 |

</div>

## Conteúdos

Este repositório contém:

- [Relatório](./report.pdf)
- [Enunciado](./enunciado.pdf)
- [Código-Fonte](./Code/src/)
- [sd23.jar (fornecido pela equipe docente)](./Code/src/sd23.jar)

## Nota Obtida: 19.6 valores