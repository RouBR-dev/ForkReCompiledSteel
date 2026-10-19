<div align="center"> <img width="1920" height="480" alt="banner" src="https://github.com/user-attachments/assets/62212c07-f09d-4a5a-9107-ed1f24c96e2a" />

ReCompiled Steel — fork Android

Versão Android: 0.0.1 fix · arm64-v8a · Vulkan

Projeto comunitário baseado no 

ReCompiledSteel oficial.
Este repositório contém modificações próprias, principalmente no launcher e no comportamento Android.

</div> <p align="center">
<img width="1600" height="720" alt="Menu do launcher ReCompiled Steel para Android" src="https://github.com/user-attachments/assets/9294b647-0ccf-4290-bc8e-c8af804ffb0d" />
</p>


Não é o repositório oficial. O projeto original, sua história e os créditos de sua equipe pertencem ao upstream indicado acima. Este fork não é afiliado a Yuke's, DreamWorks ou Microsoft, nem é endossado por essas empresas.

Sobre o projeto

ReCompiled Steel é uma recompilação estática de Real Steel, jogo de Xbox 360 lançado em 2011. O código do jogo é recompilado para código nativo com o SDK ReXGlue, e um renderer Vulkan próprio desenha o jogo. Não é um emulador de Xbox 360.

Esta cópia parte do projeto de 

tokeneffortports/ReCompiledSteel e concentra as alterações adicionais no Android. As funções e os componentes originais que não foram modificados continuam sendo trabalho do projeto upstream. Para ver a descrição original completa, consulte o 

README oficial.

O repositório não contém o jogo, o Title Update, DLC, saves ou arquivos proprietários. Para jogar e compilar, use apenas os arquivos de uma cópia que você tenha direito de utilizar.

O que muda neste fork em relação ao upstream

O upstream já trazia a versão Android arm64 com SDL3 e Vulkan, a instalação dos arquivos do jogo pelo launcher, controles por toque/controle físico, diagnósticos, seleção opcional de driver Vulkan e opções gráficas dentro do jogo. Também já oferecia ajustes como resolução reduzida, multidão, qualidade das texturas e manutenção da velocidade normal. Esses recursos de base não são apresentados aqui como criações deste fork.

As alterações próprias desta cópia são principalmente as seguintes:

•
Launcher redesenhado com Material 3: tela inicial reorganizada com barra superior, cartões de estado, botões, progresso e telas de gráficos e driver com componentes Material para Android.

•
Retorno ao menu após falhas: o jogo roda no processo Android separado :game, enquanto o launcher permanece em seu próprio processo. Se o processo do jogo terminar inesperadamente, o Android pode voltar ao launcher, que registra a etapa da falha e permite gerar diagnósticos. Erros ao selecionar ou instalar arquivos são apresentados no launcher para permitir uma nova tentativa. Isso reduz o risco de um crash nativo fechar também o menu, mas não impede encerramentos causados pelo Android, por falta extrema de memória ou falhas gerais do sistema.

•
Sem troca automática de driver após uma falha: o launcher não pergunta mais se deve voltar ao driver do sistema e não o troca por conta própria. O driver escolhido permanece até que o usuário o altere manualmente nas configurações.

•
Mais controles de desempenho no launcher: a resolução interna pode ser reduzida até 180p; há opções para antisserrilhamento, filtragem de textura, multidão, qualidade das texturas e profundidade de campo; e podem ser escolhidos limites de 30, 45, 60, 90 ou 120 FPS, ou sem limite.

•
Pular quadros do renderer: opção numérica experimental de 0 a 3. Ela descarta frames enviados ao renderer Vulkan, mas não pula a simulação, a lógica do jogo, os controles nem o áudio.

•
Ajustes Android de apresentação e desempenho: o renderer solicita pelo menos três imagens de swapchain, usa apresentação FIFO no Android e solicita 60 Hz à janela. O código também tenta dar prioridade às threads do jogo e do renderer e solicitar modo de desempenho sustentado. O Android ou o driver pode ignorar ou limitar esses pedidos.

•
Integração com Android Studio: além dos scripts Android do projeto original, esta cópia inclui configuração Gradle para abrir a raiz do repositório no Android Studio e compilar o APK pelo módulo app.

A tela de configurações grava os valores em RealSteel/realsteel_settings.cfg; as alterações gráficas são aplicadas no próximo início do jogo. As opções experimentais não garantem maior desempenho. Em particular, diminuir a resolução ajuda principalmente quando a GPU é o gargalo; pular frames reduz o trabalho do renderer, mas pode deixar a imagem entrecortada. Nenhuma dessas opções acelera a execução da lógica do jogo quando o limite é a CPU.

Requisitos

Para instalar e jogar, você precisa de um dispositivo Android arm64-v8a com driver Vulkan compatível com as extensões exigidas pelo renderer. O projeto foi testado em poucos aparelhos; compatibilidade varia entre fabricantes, GPUs Mali/Adreno, versões do Android e drivers. Ter Vulkan no aparelho, por si só, não garante que o jogo será compatível nem que rodará a 60 FPS.

Você também precisa dos arquivos da sua própria cópia de Real Steel para Xbox 360:

•
pacote do jogo em 000D0000;

•
Title Update em 000B0000, necessário para esta recompilação;

•
conteúdo adicional em 00000002, opcional.

O identificador do jogo é 584111E0. O caminho comum no armazenamento do Xbox é Content/0000000000000000/584111E0/. O launcher procura os pacotes nas pastas selecionadas; depois da instalação, os arquivos são organizados em RealSteel/.

Instalação no Android

1.
Copie para o celular ou cartão SD a pasta que contém os pacotes de Real Steel extraídos do seu Xbox 360.

2.
Instale o APK arm64 e abra o aplicativo. Se o Android solicitar, permita a instalação de aplicativos provenientes do gerenciador de arquivos usado.

3.
No launcher, conceda o acesso a arquivos solicitado pelo Android.

4.
Toque em Instalar arquivos do jogo e selecione a pasta com o jogo e o Title Update. DLCs podem ser incluídos se você os tiver.

5.
Aguarde a cópia e validação dos arquivos e toque em Iniciar.

O jogo e o Title Update são indispensáveis. Sem eles, o repositório não produz uma instalação jogável. Os pacotes originais não são distribuídos junto com o APK nem com o código-fonte.

Os controles compatíveis conectados ao Android podem ser usados no jogo. Quando não há controle conectado, o jogo pode apresentar controles de toque, conforme as configurações internas. Saves e conteúdo do usuário ficam dentro de RealSteel/user/; logs e diagnósticos ficam em RealSteel/logs/.

Opções de gráficos e desempenho

As opções são atalhos do launcher para valores que serão usados pelo renderer e pelo jogo. Os valores podem ser restaurados para os recomendados para Android.

•
Resolução interna: de 180p a 2880p, incluindo 240p, 360p, 480p, 540p, 720p, 900p, 1080p, 1440p e 4K. Resoluções menores reduzem o número de pixels renderizados, mas deixam a imagem mais suave.

•
Antisserrilhamento: desligado, FXAA, MSAA ou MSAA + FXAA. O MSAA pode ser mais pesado em dispositivos móveis.

•
Filtro de textura: trilinear/desativado ou anisotrópico de 2x a 16x.

•
Multidão: pode ser desligada para reduzir trabalho gráfico nas arenas.

•
Qualidade das texturas: completa ou metade. A opção pela metade usa menos memória, com perda de nitidez.

•
Profundidade de campo: pode ser ligada ou desligada.

•
Espera pelo renderer: a opção ativada evita aguardar o renderer em certos caminhos e pode descartar frames atrasados. Ela ajuda apenas quando o renderer está atrasando a thread do jogo; não reduz o tempo da lógica CPU-bound.

•
Pular quadros: 0 não pula; 1 renderiza um e pula o seguinte; 2 renderiza um e pula dois; 3 renderiza um e pula três. É uma opção experimental, pode deixar o movimento entrecortado e pode perder atualizações visuais transitórias. Se houver artefatos, volte a 0.

•
Limite de velocidade/FPS: 30, 45, 60, 90, 120 ou sem limite. Os limites de 90/120 e a opção sem limite não fazem o processador executar o jogo mais rápido; só têm efeito se o aparelho conseguir completar mais frames.

Importante sobre câmera lenta: se a CPU não conseguir executar a lógica recompilada no tempo necessário, opções de resolução ou de frameskip não eliminam esse gargalo. Elas podem ajudar quando a GPU ou o renderer limita a apresentação, mas não são um overclock e não prometem 60 FPS em aparelhos específicos, inclusive Unisoc T606, Mali ou Adreno.

Driver gráfico

Por padrão, o jogo usa o driver Vulkan fornecido pelo sistema. Em aparelhos Qualcomm/Adreno, o launcher também oferece a seleção manual de um driver personalizado compatível. Carregar um driver de terceiros pode causar travamentos, corrupção visual ou comportamento inesperado; essa operação é por conta e risco do usuário e não é garantida pelo projeto.

Se uma inicialização falhar enquanto um driver personalizado estiver selecionado, o launcher não altera a seleção automaticamente nem exibe a antiga pergunta de confirmação. Para testar o driver do sistema, abra Driver gráfico e escolha Voltar ao driver do sistema manualmente. Os logs identificam o driver usado quando essa informação está disponível.

Diagnósticos e solução de problemas

Se o jogo não iniciar ou apresentar erro, volte ao launcher e use Salvar diagnósticos. O arquivo gerado fica em RealSteel/logs/ e reúne registros úteis para investigar a inicialização, o renderer e eventuais crashes. Ao relatar um problema, informe o modelo do aparelho, versão do Android, GPU/driver, opções gráficas usadas e as etapas que levaram à falha.

A opção de diagnósticos não envia dados automaticamente. Compartilhe o arquivo somente com quem você deseja que o receba e remova informações pessoais antes de publicar logs abertamente.

Compilar pelo Android Studio

A raiz do repositório contém settings.gradle.kts e o módulo Gradle app. O Gradle chama CMake para compilar o código nativo. O codegen ReXGlue e a geração do cache de shaders continuam dependendo dos arquivos e ferramentas necessários ao projeto original.

O fluxo requer Linux x64 ou Ubuntu no WSL2, Android Studio, JDK 17, Android SDK Platform 35, Build-Tools 35.0.0, NDK 29.0.14206865, CMake 3.25 ou superior, Ninja, Clang, Python 3, Git e conexão à internet para obter dependências. Também são necessários os arquivos de jogo autorizados em work/, o SDK ReXGlue e o cache de shaders gerado pelo XenosRecomp.

Em linhas gerais:

1.
Prepare os arquivos do jogo em work/, conforme docs/building.md.

2.
Gere o cache de shaders com tools/shaders/setup_xenosrecomp.py e prepare o SDK Android ReXGlue com android/build_sdk.sh.

3.
Configure os caminhos locais copiando local.properties.example para local.properties e ajustando os valores para a sua máquina.

4.
Abra no Android Studio a pasta raiz que contém settings.gradle.kts, configure o JDK 17 e aguarde o Gradle Sync.

5.
Compile a variante Release com :app:assembleRelease ou pelo terminal:

Bash


./gradlew :app:assembleRelease





O APK é gerado em app/build/outputs/apk/release/. A tarefa checkInputs informa alguns pré-requisitos ausentes. A compilação Release deste pacote usa uma chave de assinatura de debug para instalação manual; gere e configure uma chave própria antes de qualquer publicação formal.

O guia detalhado em português está em COMO_COMPILAR_ANDROID_STUDIO.md. Para a visão geral das ferramentas e do fluxo de recompilação original, consulte docs/building.md e o 

guia Android oficial do upstream.

Créditos

Este fork deriva do projeto 

ReCompiledSteel de tokeneffortports. A recompilação estática, o renderer Vulkan original, o código do jogo, os recursos de multiplayer/mods e o porte Android de base são atribuídos ao upstream e aos colaboradores listados abaixo. As alterações Android descritas neste README pertencem a esta cópia modificada e não devem ser atribuídas ao projeto oficial.

•
Real Steel (2011), de Yuke's Co., Ltd., para Xbox Live Arcade. Real Steel é marca registrada e copyright de DreamWorks II Distribution Co., LLC. Este é um projeto de fãs não oficial, sem vínculo ou endosso de Yuke's, DreamWorks ou Microsoft, e não inclui arquivos dessas empresas.

•
ReXGlue SDK, de tomcl7 e colaboradores: recompilador estático e runtime usados pelo projeto, que se apoiam no trabalho do Xenia, XenonRecomp e do recompilador de rexdex.

•
XenosRecomp, de hedge-dev, tradutor de shaders do Xbox 360 usado com thirdparty/XenosRecomp-realsteel.patch, e Unleashed Recompiled, referência de engenharia para ports recompilados.

•
Skate 3 Android port, de luisxl15, cujas notas sobre Android ajudaram o porte original.

•
Bibliotecas de terceiros: volk, Vulkan-Headers, zstd, smol-v, libadrenotools (Android) e, por meio do SDK, Dear ImGui, SDL3, FFmpeg e outras. Cada componente mantém sua própria licença; consulte packaging/licenses.

•
A compreensão dos formatos do Xbox 360 contou com o Xenia, o Free60 e a comunidade homebrew. Os formatos próprios de Real Steel — arquivos PAC, compressão BPE e modelos JBOY — foram estudados no projeto original.

•
O README oficial registra que a engenharia do projeto upstream foi feita com Claude, da Anthropic, por meio do Claude Code. Esse crédito se refere ao upstream, não é uma afirmação sobre a autoria das alterações deste fork.

•
thefixinhixon/RealSteelRecomp é uma recompilação independente do mesmo jogo; segundo o upstream, os projetos não compartilham código.

Licença

O código abrangido por este repositório está sob GNU GPL-3.0, conforme LICENSE. As bibliotecas de terceiros mantêm suas próprias licenças, documentadas em packaging/licenses/.

A licença do código não concede direitos sobre Real Steel, seus personagens, marcas, imagens, músicas, pacotes do Xbox 360 ou Title Update. Nenhum desses arquivos é incluído aqui. O usuário é responsável por utilizar arquivos obtidos de forma autorizada.

