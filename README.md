<div align="center"> <img width="1920" height="480" alt="Banner do ReCompiled Steel" src="https://github.com/user-attachments/assets/62212c07-f09d-4a5a-9107-ed1f24c96e2a" />

ReCompiled Steel — Fork Android

Real Steel (Xbox 360, 2011 ) recompilado para Android arm64, com renderer Vulkan e launcher Material 3.

Versão: 0.0.1 fix · Plataforma: Android arm64-v8a

Projeto oficial de origem: 

tokeneffortports/ReCompiledSteel

</div> <p align="center">
<img width="1600" height="720" alt="Menu do launcher Android com interface Material 3" src="https://github.com/user-attachments/assets/9294b647-0ccf-4290-bc8e-c8af804ffb0d" />
</p>


Este repositório é um fork comunitário do projeto oficial. Não é afiliado a Yuke's, DreamWorks ou Microsoft, nem é endossado por essas empresas. Nenhum arquivo do jogo é distribuído aqui.

Sobre o projeto

O código de Real Steel é recompilado estaticamente para código nativo usando o SDK ReXGlue. Um renderer Vulkan próprio apresenta o jogo; não há emulação da GPU Xbox 360 nem tradução de instruções durante a partida. Esta cópia mantém a base do 

projeto oficial e concentra suas modificações adicionais no launcher e no comportamento Android.

A recompilação original, o renderer Vulkan de base e os recursos desenvolvidos pelo upstream continuam sendo atribuídos a seus autores. As diferenças específicas deste fork estão descritas em O que este fork modifica. Para a descrição completa do projeto original e de suas versões desktop, consulte o 

README oficial do tokeneffortports/ReCompiledSteel.

O que você precisa

•
Um dispositivo Android arm64-v8a com driver Vulkan e as extensões necessárias ao renderer. A compatibilidade depende do aparelho, da versão do Android e do driver; Vulkan disponível não garante que o jogo iniciará ou manterá 60 FPS.

•
Sua própria cópia de Real Steel para Xbox 360, com o jogo e o Title Update. Os DLCs são opcionais.

Arquivo/conteúdo
Identificador
Necessário
Real Steel, Xbox Live Arcade
000D0000
Sim
Title Update
000B0000
Sim
Conteúdo adicional
00000002
Não
Title ID
584111E0
—




No console, os arquivos normalmente ficam em Content/0000000000000000/584111E0/. Use somente arquivos de uma cópia que você tenha direito de utilizar. O repositório, o APK e o ZIP-fonte não contêm o jogo, o Title Update, os DLCs, saves ou outros arquivos proprietários.

Instalação no Android

1.
Copie para o celular ou cartão SD a pasta com os arquivos do jogo extraídos do seu Xbox 360.

2.
Instale o APK arm64 e abra ReCompiled Steel. Se solicitado, autorize a instalação pelo gerenciador de arquivos.

3.
No launcher, conceda o acesso a arquivos solicitado pelo Android.

4.
Toque em Instalar arquivos do jogo e escolha a pasta com o jogo e o Title Update. Inclua os DLCs opcionais se os tiver.

5.
Aguarde a cópia e a validação e toque em Iniciar.

O launcher organiza os dados em RealSteel/. Saves e dados do usuário ficam em RealSteel/user/; logs e arquivos de diagnóstico ficam em RealSteel/logs/.

Um controle compatível pode ser usado. Sem controle conectado, o jogo pode mostrar controles de toque; sua aparência e posição são configuradas nas opções internas do jogo.

Windows e Linux

O código-base oficial também inclui versões para Windows, Linux e Steam Deck. As alterações e este README são focados no fork Android. Consulte o 

repositório oficial para as instruções atuais de instalação e execução desktop.

Recursos

Renderer e imagem

A renderização é feita por Vulkan usando shaders do jogo traduzidos antes da execução. No Android, o launcher deste fork expõe:

•
resolução interna de 180p a 2880p, incluindo 240p, 360p, 480p, 540p, 720p, 900p, 1080p, 1440p e 4K;

•
antisserrilhamento desligado, FXAA, MSAA ou MSAA + FXAA;

•
filtragem de textura trilinear ou anisotrópica de 2x a 16x;

•
multidão ligada/desligada, texturas completas/metade e profundidade de campo ligada/desligada;

•
presets rápidos para priorizar desempenho ou qualidade.

O upstream já inclui opções gráficas dentro do jogo, como resolução reduzida, qualidade de textura, multidão e manutenção da velocidade normal. O fork acrescenta controles dessas opções no launcher e oferece o preset de 180p.

Opções para aparelhos mais fracos

O launcher inclui controles experimentais para o caminho Android:

•
Não esperar pelo renderer / manter a velocidade: permite avançar sem aguardar certas filas do renderer, podendo descartar frames atrasados. Ajuda apenas se o renderer for o gargalo.

•
Pular quadros: 0 desativado; 1 pula um a cada dois; 2 renderiza um e pula dois; 3 renderiza um e pula três. A opção só afeta os frames enviados ao renderer Vulkan; não pula a simulação, a lógica, os controles ou o áudio.

•
Limite de FPS: 30, 45, 60, 90, 120 ou sem limite. Os limites mais altos só funcionam se o aparelho conseguir executar frames adicionais.

O frame skip pode deixar o movimento entrecortado ou perder atualizações visuais transitórias. Desative-o se notar artefatos. Reduzir a resolução pode ajudar quando a GPU é o gargalo. Nenhum desses ajustes acelera a lógica limitada pela CPU, faz overclock ou garante a correção de câmera lenta em aparelhos como Unisoc T606, Mali ou Adreno.

O que este fork modifica

Em comparação com a versão Android documentada pelo upstream, esta cópia acrescenta ou altera:

•
Launcher Material 3: nova tela inicial com barra superior, cartões de estado, botões e submenus de gráficos e driver.

•
Isolamento do processo do jogo: a atividade do jogo roda no processo Android :game, separado do launcher. Se o processo nativo terminar inesperadamente, o Android pode retornar ao menu, que registra a etapa da falha e permite salvar diagnósticos. Erros de seleção ou instalação de arquivos aparecem no launcher para permitir nova tentativa. Isso não impede o Android de encerrar o aplicativo por falta extrema de memória ou falha geral do sistema.

•
Comportamento do driver após falha: o launcher não pergunta mais se o usuário quer voltar ao driver do sistema e não o seleciona automaticamente. A troca continua disponível manualmente nas configurações.

•
Integração Gradle/Android Studio: é possível abrir a raiz do repositório no Android Studio e compilar pelo módulo app, além do fluxo por scripts Android do projeto-base.

•
Ajustes de apresentação para Android: o renderer solicita pelo menos três imagens de swapchain, usa FIFO e solicita 60 Hz à janela. Também tenta ajustar a prioridade das threads do jogo e do renderer e solicitar modo de desempenho sustentado. O sistema ou o driver pode limitar ou ignorar esses pedidos.

A tela de configurações grava os valores em RealSteel/realsteel_settings.cfg; eles são aplicados no próximo início do jogo. Os padrões Android incluem antisserrilhamento desligado, anisotropia 2x, velocidade normal com o renderer sem espera, frame skip desativado e limite de 60 FPS.

Recursos herdados do projeto oficial

Este fork mantém componentes e recursos do projeto upstream que não foram alterados, incluindo o código recompilado do jogo, o renderer Vulkan de base, suporte a controles, o menu de configurações interno e recursos originais como multiplayer, mods, conquistas e DLCs quando disponíveis na versão e com os arquivos necessários. Para a lista completa e documentação desses recursos, consulte o 

README oficial.

Driver gráfico

Por padrão, o jogo usa o driver Vulkan do sistema. Em aparelhos Qualcomm/Adreno, o launcher permite selecionar manualmente um driver personalizado compatível, carregado por meio do libadrenotools. Drivers externos podem causar travamentos, erros de imagem ou comportamento inesperado; o uso é opcional, por conta e risco do usuário e não é garantido pelo projeto.

Se uma inicialização falhar enquanto um driver personalizado estiver selecionado, o launcher não altera a seleção automaticamente nem exibe a antiga confirmação de retorno. Para testar o driver do sistema, abra Driver gráfico e escolha Voltar ao driver do sistema manualmente.

Por que recompilação, e não Xenia?

O Xenia é um emulador que executa muitos jogos de Xbox 360. ReCompiled Steel é voltado a um único jogo: o código de Real Steel é recompilado antes da execução e vira um programa nativo. Não há tradução de instruções durante a partida nem uma GPU Xbox 360 emulada. Para jogar vários títulos de Xbox 360, use um emulador apropriado; este projeto é uma recompilação dedicada a Real Steel.

Problemas conhecidos

•
Compatibilidade e desempenho variam conforme o aparelho e o driver Vulkan. O projeto não foi validado em todos os dispositivos Android.

•
Redução de resolução e frame skip não corrigem gargalos de CPU; frame skip pode reduzir a suavidade visual.

•
Driver Vulkan personalizado pode impedir o início ou causar instabilidade. Volte manualmente ao driver do sistema para comparar.

•
O processo separado reduz o risco de o crash nativo fechar o launcher, mas não é uma garantia contra encerramentos provocados pelo Android.

•
Recursos herdados, como multiplayer pela internet, podem depender de rede, servidor e configuração do usuário. Consulte a documentação oficial para detalhes.

Relatar um problema

No launcher, use Salvar diagnósticos e anexe o arquivo criado em RealSteel/logs/. Informe também o modelo do dispositivo, a versão do Android, a GPU e o driver em uso, as opções gráficas escolhidas e o que você fazia quando ocorreu o problema. Os diagnósticos não são enviados automaticamente; revise o arquivo antes de publicá-lo para remover dados pessoais.

Compilar pelo Android Studio

O código recompilado e o cache de shaders são gerados a partir dos arquivos do jogo e não são incluídos no repositório. A compilação Android requer Linux x64 ou Ubuntu no WSL2, Android Studio, JDK 17, Android SDK Platform 35, Build-Tools 35.0.0, NDK 29.0.14206865, CMake 3.25 ou superior, Ninja, Clang, Python 3, Git e acesso à internet para obter dependências.

1.
Prepare os arquivos autorizados do jogo em work/, seguindo docs/building.md.

2.
Gere o cache de shaders com tools/shaders/setup_xenosrecomp.py e prepare o SDK Android ReXGlue com android/build_sdk.sh.

3.
Copie local.properties.example para local.properties e ajuste os caminhos da sua máquina.

4.
Abra no Android Studio a pasta raiz que contém settings.gradle.kts, configure o JDK 17 e aguarde o Gradle Sync.

5.
Compile a variante Release pelo Android Studio ou execute:

Bash


./gradlew :app:assembleRelease





O APK é gerado em app/build/outputs/apk/release/. A tarefa checkInputs informa alguns pré-requisitos ausentes. A compilação Release está configurada para instalação manual com chave de debug; use uma chave própria para uma publicação formal.

Guias: COMO_COMPILAR_ANDROID_STUDIO.md e 

documentação Android oficial.

Créditos

Este fork deriva de 

tokeneffortports/ReCompiledSteel. A base do projeto, a recompilação estática e o porte Android original são creditados ao upstream e aos colaboradores abaixo.

•
Real Steel (2011), de Yuke's Co., Ltd., para Xbox Live Arcade. Real Steel é marca registrada e copyright de DreamWorks II Distribution Co., LLC. Este é um projeto de fãs não oficial, sem vínculo ou endosso de Yuke's, DreamWorks ou Microsoft, e não inclui arquivos dessas empresas.

•
ReXGlue SDK, de tomcl7 e colaboradores: recompilador estático e runtime do jogo, com base no trabalho do Xenia, XenonRecomp e do recompilador de rexdex.

•
XenosRecomp, de hedge-dev, tradutor de shaders do Xbox 360 usado com thirdparty/XenosRecomp-realsteel.patch, e Unleashed Recompiled, referência para ports recompilados.

•
Skate 3 Android port, de luisxl15, cujas notas sobre Android ajudaram o porte original.

•
Bibliotecas de terceiros: volk, Vulkan-Headers, zstd, smol-v, libadrenotools (Android) e, por meio do SDK, Dear ImGui, SDL3, FFmpeg e outras. Consulte as licenças em packaging/licenses/THIRD-PARTY-LICENSES.txt.

•
Xenia, Free60 e a comunidade homebrew contribuíram para a compreensão dos formatos do Xbox 360. Os formatos próprios de Real Steel — PAC, compressão BPE e modelos JBOY — foram estudados no projeto original.

•
O README oficial registra a engenharia do upstream com Claude, da Anthropic, por meio do Claude Code. Este crédito se refere ao projeto oficial.

•
thefixinhixon/RealSteelRecomp é uma recompilação independente do mesmo jogo; segundo o upstream, os dois projetos não compartilham código.

Licença

O código coberto por este repositório está sob GNU GPL-3.0, conforme LICENSE. Componentes de terceiros mantêm suas próprias licenças. A licença do código não concede direitos sobre o jogo, marcas, imagens, músicas, Title Update ou qualquer arquivo proprietário de Real Steel. Nenhum desses arquivos é distribuído aqui.

