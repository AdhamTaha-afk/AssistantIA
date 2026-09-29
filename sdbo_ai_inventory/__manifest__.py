{
    'name': 'SDBO AI Inventory Assistant',
    'version': '1.0',
    'summary': 'AI Assistant Module for Odoo 19 Inventory Management',
    'category': 'Inventory',
    'author': 'SDBO',
    'depends': ['base', 'stock', 'web'],
    'data': [],
    'assets': {
        'web.assets_backend': [
            'sdbo_ai_inventory/static/src/js/sodo-chat-plugin.js',
        ],
    },
    'installable': True,
    'application': True,
    'auto_install': False,
}
