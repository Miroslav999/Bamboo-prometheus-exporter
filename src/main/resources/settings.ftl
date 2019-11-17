<html>
<head>
    <meta name="decorator" content="atl.admin">
    <title>Prometheus settings</title>
</head>
<body>
<section id="content" role="main">
    <header class="aui-page-header">
    </header>
    <div class="aui-page-panel">
        <div class="aui-page-panel-inner">
            <section class="aui-page-panel-content">
                 <div id="base-form">
                    [@ww.form
                        action="savesettings"
                        id="saveSettingsForm"
                        submitLabelKey='Save'
                    ]
                        [@ww.textfield
                            labelKey="Prometheus url"
                            name="url"
                            required="false"
                        /]
                    [/@ww.form]
                </div>
                </div>
            </section>
        </div>
    </div>
</section>
</body>
</html>