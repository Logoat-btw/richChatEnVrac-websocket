const fs = require("fs"); // module node d'accÃ¨s au SGF
const path = require("path"); // module node de manipulation de chemins de fichiers
const webpack = require("webpack"); // webpack
const HtmlWebpackPlugin = require("html-webpack-plugin"); // Plugin de crÃ©ation HTML
const FaviconsWebpackPlugin = require("favicons-webpack-plugin"); // Module de gÃ©nÃ©ration des icones par plateformes
const MiniCssExtractPlugin = require("mini-css-extract-plugin"); // Module de gÃ©nÃ©ration de CSS sous forme de fichier css (pour la prod)
const CssMinimizerPlugin = require("css-minimizer-webpack-plugin"); // Module de minimisation du css (pour la prod)
const autoprefixer = require("autoprefixer"); // Utilise dans la conversion de Sass vers Css
const { BundleAnalyzerPlugin } = require("webpack-bundle-analyzer"); // analyser de bundle (pour la prod)

const {
    name,
    appTitle,
    description,
    keywords,
    author,
} = require("./package.json"); // info gÃ©nÃ©rale de l'app (utilisÃ© pour le build et injectÃ© comme var d'environnement pour certaines)
const babelConfig = require("./babel.config"); // Info de config de babel (utilise pour le traitement de fichiers js/jsx)

/**
 * CONSTANTES, injectables depuis l'environnement (utile notamment dans un build docker)
 */
const PUBLIC_PATH = process.env.PUBLIC_PATH ?? "/"; // url de base de l'appli
const API_PATH = process.env.API_PATH ?? "http://localhost:8080/api/v1";

function generateStyleRuleUse({
    production = false,
    useModules = false,
    useSass = false,
} = {}) {
    /*
  SchÃ©ma Modules de build CSS
  Si SASS :
    - sass-loader (conversion sass -> css)
    - puis postcss-loader (converti syntaxe moderne/futur of css en css standard + usage de autoprefixer pour injecter les prefixes de rÃ¨gles CSS navigateur)
  Puis dans tous les cas :
    - css-loader : interprete les directive css @import et url() comme et les rÃ©soud, et au besoin, applique la transformation en module (renommage de classe css)
    - puis
      - si prod : MiniCssExtractPlugin: gÃ©nÃ¨re un fichier CSS sÃ©parÃ©
      - ou si dev : utilise style-loader pour injecter le CSS sous forme de balises <style>
  */
    let modules = [
        // Injection du CSS dans un fichier sÃ©parÃ© en PROD ou injectÃ© en DEV
        production
            ? MiniCssExtractPlugin.loader
            : { loader: "style-loader", options: { injectType: "styleTag" } },
        // InterprÃªte le CSS en CommonJS et utilise les modules le cas Ã©chant
        { loader: "css-loader", options: { modules: useModules } },
        {
            // Converti syntaxe moderne/futur of css en css standard
            // Utilisation de autoprefixer pour injecter les directive css vendor specific Ã  partir des notres
            loader: "postcss-loader",
            options: {
                postcssOptions: {
                    plugins: [autoprefixer],
                },
            },
        },
    ];
    if (useSass) {
        modules = [
            ...modules,
            {
                // Charge un fichier sass et le converti en css
                loader: "sass-loader",
                options: {
                    sassOptions: {
                        // Rend les avertissements dÃ» Ã  l'usage de directives sass prochainement dÃ©prÃ©ciÃ©es dans Bootstrap silencieux
                        silenceDeprecations: [
                            "mixed-decls",
                            "color-functions",
                            "global-builtin",
                            "import",
                        ],
                    },
                },
            },
        ];
    }
    return modules;
}

module.exports = (env, argv) => {
    const PRODUCTION_MODE = argv.mode === "production";
    const DEV_MODE = argv.mode === "development";

    // CrÃ©ation de la configuration de base
    const CONFIG = {
        mode: PRODUCTION_MODE ? "production" : "development",
        // Environnement cible du dÃ©ploiement
        target: "web",
        // Point d'entrÃ©e de l'application
        entry: "./src/index.jsx",
        // Sortie
        output: {
            path: path.join(__dirname, "build"), // chemin obligatoirement absolu
            filename: PRODUCTION_MODE
                ? "[name].[contenthash].bundle.js"
                : "[name].bundle.js", // Ajout un hash pour s'assurer le telechargement du nouveau code produit par le navigateur
            publicPath: PUBLIC_PATH,
            clean: true, // efface le contenu du dossier de sortie avant regÃ©nÃ©ration
        },
        // plugins de construction
        plugins: [
            // DÃ©finition de variables d'environnement injectable dans le code-source
            new webpack.DefinePlugin({
                APP_ENV_APP_PUBLIC_PATH: JSON.stringify(PUBLIC_PATH),
                APP_ENV_APP_TITLE: JSON.stringify(appTitle),
                APP_ENV_API_PATH: JSON.stringify(API_PATH),
            }),

            // GÃ©nÃ©ration du fichier index.html Ã  partir d'un template
            new HtmlWebpackPlugin({
                template: "./src/index.html",
                filename: "index.html",
                inject: "body",
                title: appTitle,
                favicon: "./src/assets/favicon.ico",
                meta: {
                    description: description ?? "no description",
                    keywords: keywords?.join(", ") ?? "",
                    author: author ?? "unknown",
                },
            }),
            // GÃ©nÃ©ration des icones et du manifest
            new FaviconsWebpackPlugin({
                // mode: 'webapp',
                // devMode: 'webapp',
                // Your source logo (required)
                logo: "./src/assets/logo.png",
                // Cross-build cache
                cache: true,
                // Prefix path for generated assets
                prefix: "assets/",
                // Inject html links/metadata (requires html-webpack-plugin).
                inject: true,
                // Favicons configuration options (see below)
                favicons: {
                    appName: appTitle, // Your application's name. `string`
                    appShortName: name, // Your application's short_name. `string`. Optional. If not set, appName will be used
                    // appDescription: 'Module de saisie des prÃ©sences pour l\'IUT de Laval', // Your application's description. `string`
                    dir: "auto", // Primary text direction for name, short_name, and description
                    lang: "fr-FR", // Primary language for name and short_name
                    background: "black", // Background colour for flattened icons. `string`
                    theme_color: "#FF407D", // Theme color user for example in Android's task switcher. `string`
                    // start_url: `/${IDX_STUDENT_FILE_NAME}?homescreen=1`,
                },
            }),
        ],
        // dÃ©finit comment les modules vont Ãªtre chargÃ©s
        // //ajoute les extensions .jsx et .scss aux extensions gÃ©rÃ©es
        resolve: {
            extensions: [".js", ".json", ".jsx", ".scss", ".wasm", ".mjs"],
        },
        // modules de configuration selon le type de fichier rencontrÃ©
        module: {
            rules: [
                {
                    // Gestion des fichiers css
                    test: /\.css$/i,
                    exclude: /draft-js\/dist\/Draft\.css$/i,
                    use: generateStyleRuleUse({
                        production: PRODUCTION_MODE,
                        useModules: true,
                    }),
                },
                {
                    // Gestion du ficheir draft-js
                    test: /draft-js\/dist\/Draft\.css$/i,
                    use: generateStyleRuleUse({ production: PRODUCTION_MODE }),
                },
                {
                    // Gestion des fichiers sass de l'appli (modules css par dÃ©faut)
                    test: /\.s[ac]ss$/i,
                    exclude: /bootstrap-config\.s[ac]ss$/i,
                    use: generateStyleRuleUse({
                        production: PRODUCTION_MODE,
                        useModules: true,
                        useSass: true,
                    }),
                },
                {
                    // Gestion du fichier sass de chargement de chargement / custome de boostrap
                    test: /bootstrap-config\.s[ac]ss$/i,
                    use: generateStyleRuleUse({
                        production: PRODUCTION_MODE,
                        useSass: true,
                    }),
                },
                {
                    // Gestion des fichiers images
                    test: /\.(png|svg|jpg|jpeg|gif)$/i,
                    type: "asset/resource", // le module asset Ã©met un fichier sÃ©parÃ© du bundle et exporte son url
                },
                {
                    // Gestion des polices d'Ã©criture
                    test: /\.(woff|woff2|eot|ttf|otf)$/i,
                    type: "asset/resource", // le module asset Ã©met un fichier sÃ©parÃ© du bundle et exporte son url
                },
                {
                    // Gestion du code-source js et jsx en utilisant babel pour
                    // la transpilation
                    // Exclut les fichiers js de node_modules du passage par babel
                    test: /\.jsx?$/,
                    exclude: /(node_modules)/,
                    use: {
                        loader: "babel-loader",
                        options: babelConfig, // configuration sÃ©parÃ© car rÃ©-utilisÃ© avec eslint
                    },
                },
            ],
        },
    };

    // Modification spÃ©cifique de la config selon le mode
    if (PRODUCTION_MODE) {
        // Ajout de plugins pour la production
        CONFIG.plugins.push(
            // SÃ©paration des CSS du code JS dans des fichiers sÃ©parÃ©s
            new MiniCssExtractPlugin({
                filename: "[name].[contenthash].css",
            }),
            // Injections des licences
            new webpack.BannerPlugin(fs.readFileSync("./LICENSE", "utf8")),
            // Creation de rapports statistiques sur la taille des bundles
            new BundleAnalyzerPlugin({
                analyzerMode: "static",
                reportFilename: path.join(__dirname, "buildInfos/report.html"),
                generateStatsFile: true,
                statsFilename: path.join(__dirname, "buildInfos/stats.json"),
            })
        );
        CONFIG.devtool = "source-map";
        // Optimisation du build
        CONFIG.optimization = {
            moduleIds: "deterministic", // les ids de modules sont calculÃ©s de maniÃ¨re Ã  ne pas changer si le contenu du module ne change pas
            runtimeChunk: "single", // CrÃ©er un seul runtime de code pour l'ensemble des chunks
            splitChunks: {
                // Met Ã  part les codes des biblio tierces (module vendors)
                cacheGroups: {
                    vendor: {
                        test: /[\\/]node_modules[\\/]/,
                        name: "vendors",
                        chunks: "all",
                    },
                },
            },
            minimizer: [
                "...", // utilise les paramÃ¨tres par dÃ©faut des minimzer (TerserPlugin pour minifier et minimiser JS)
                new CssMinimizerPlugin(), // minimise le CSS
            ],
        };
    } else if (DEV_MODE) {
        CONFIG.devtool = "inline-source-map"; // Inject les source map dans le code pour un debug facilitÃ©
        // Serveur de dÃ©veloppement
        CONFIG.devServer = {
            port: 3000,
            host: "localhost", // Accessible uniquement d'une ip localhost (4 ou 6)
            historyApiFallback: true, // Evite d'afficher une page 404 plutot que la page index.html
            // quand on utilie HTML5 History API
            static: [
                {
                    directory: path.resolve(__dirname, "build"),
                },
            ],
            open: true, // tente d'ouvre une page navigateur une fois le serveur lancÃ©
            hot: true, // active le remplacement Ã  chaud des modules
            client: {
                // n'affiche sur le navigateur en overlay que les erreurs
                overlay: {
                    errors: true,
                    warnings: false,
                    runtimeErrors: true,
                },
            },
        };
    }

    return CONFIG;
};
